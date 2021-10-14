package com.algaworks.algafood.api.exceptionhandler;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.algaworks.algafood.domain.exception.EntidadeEmUsoException;
import com.algaworks.algafood.domain.exception.EntidadeNaoEncontradaException;
import com.algaworks.algafood.domain.exception.NegocioException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	@ExceptionHandler(EntidadeNaoEncontradaException.class)
	public ResponseEntity<?> tratarEntidadeNaoEncontradaException(
			EntidadeNaoEncontradaException e, WebRequest request) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		ProblemType problemType = ProblemType.ENTIDADE_NAO_ENCONTRADA;
		String datail = e.getMessage();
		
		
		Problema problema = createProblemBuilder(status, problemType, datail).build();
		
//		Problema problema = Problema.builder()
//				.status(status.value())
//				.type("https://algafood.com.br/entidade-nao-encontrada")
//				.title("Entidade não Encontrada")
//				.datail(e.getMessage())
//				.build();
		return handleExceptionInternal(e, problema, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(EntidadeEmUsoException.class)
	public ResponseEntity<?> tratarEntidadeEmUsoException(EntidadeEmUsoException e, WebRequest request) {
		
		return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.CONFLICT, request);
	}
	
	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<?> tratarNegocioException(NegocioException e, WebRequest request) {
		
		return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object problema , HttpHeaders headers,
			HttpStatus status, WebRequest request) {
		
		if(problema == null) {
			problema = Problema.builder()
					.title((String) problema)
					.status(status.value())
					.build();
		} else if (problema instanceof String) {
			problema = Problema.builder()
					.title((String) problema)
					.status(status.value())
					.build();
		}

		return super.handleExceptionInternal(ex, problema, headers, status, request);
	}
	
	private Problema.ProblemaBuilder createProblemBuilder(HttpStatus status, ProblemType problemType, String datail) {
		return Problema.builder()
				.status(status.value())
				.type(problemType.getUri())
				.title(problemType.getTitle())
				.datail(datail);
	}
}

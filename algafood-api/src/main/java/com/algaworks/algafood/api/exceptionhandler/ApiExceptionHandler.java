package com.algaworks.algafood.api.exceptionhandler;


import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.algaworks.algafood.domain.exception.EntidadeEmUsoException;
import com.algaworks.algafood.domain.exception.EntidadeNaoEncontradaException;
import com.algaworks.algafood.domain.exception.NegocioException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;

import antlr.collections.List;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		
		Throwable rootCause = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(ex);
		
		if( rootCause instanceof InvalidFormatException ) {
			return handleInvalidFormatException((InvalidFormatException)rootCause, headers, status, request);
		}else if (rootCause instanceof PropertyBindingException) {
	        return handlePropertyBindingException((PropertyBindingException) rootCause, headers, status, request); 
	    }
		
		ProblemType problemType = ProblemType.CORPO_NAO_LEGIVEL;
		String datail = "Erro no corpo passado na requisição, Verifique a sintaxe";
		
		
		Problema problema = createProblemBuilder(status, problemType, datail).build();
		
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}
	
	private ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request){
		
		String path = joinPath(ex.getPath());
		
		ProblemType problemType = ProblemType.CORPO_NAO_LEGIVEL;
		String datail = String.format("A propriedade '%s' recebeu o valor '%s',"
				+ "que é de um tipo inválido. Informa um valor capatível com o tipo '%s'", 
				path , ex.getValue(), ex.getTargetType().getSimpleName());
		
		Problema problema = createProblemBuilder(status, problemType, datail).build();
		
		return handleExceptionInternal(ex, problema, headers, status, request);
	}
	
	private ResponseEntity<Object> handlePropertyBindingException(PropertyBindingException ex,
	        HttpHeaders headers, HttpStatus status, WebRequest request) {

	    String path = joinPath(ex.getPath());
	    
	    ProblemType problemType = ProblemType.CORPO_NAO_LEGIVEL;
	    String detail = String.format("A propriedade '%s' não existe. "
	            + "Corrija ou remova essa propriedade e tente novamente.", path);

	    Problema problem = createProblemBuilder(status, problemType, detail).build();
	    
	    return handleExceptionInternal(ex, problem, headers, status, request);
	}  
	
	private String joinPath(java.util.List<Reference> references) {
	    return references.stream()
	        .map(ref -> ref.getFieldName())
	        .collect(Collectors.joining("."));
	}     
	
	
	@ExceptionHandler(EntidadeNaoEncontradaException.class)
	public ResponseEntity<?> tratarEntidadeNaoEncontradaException(
			EntidadeNaoEncontradaException e, WebRequest request) {
		HttpStatus status = HttpStatus.NOT_FOUND;
		ProblemType problemType = ProblemType.ENTIDADE_NAO_ENCONTRADA;
		String datail = e.getMessage();
		
		
		Problema problema = createProblemBuilder(status, problemType, datail).build();

		return handleExceptionInternal(e, problema, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(EntidadeEmUsoException.class)
	public ResponseEntity<?> tratarEntidadeEmUsoException(EntidadeEmUsoException e, WebRequest request) {
		HttpStatus status = HttpStatus.CONFLICT;
		ProblemType problemType = ProblemType.ENTIDADE_EM_USO;
		String datail = e.getMessage();
		
		Problema problema = createProblemBuilder(status, problemType, datail).build();
				
		return handleExceptionInternal(e, problema, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<?> tratarNegocioException(NegocioException e, WebRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ProblemType problemType = ProblemType.ERRO_NEGOCIO;
		String datail = e.getMessage();
		
		Problema problema = createProblemBuilder(status,problemType,datail).build();
		
		return handleExceptionInternal(e, problema, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
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

package com.algaworks.algafood.api.exceptionhandler;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.algaworks.algafood.domain.exception.EntidadeEmUsoException;
import com.algaworks.algafood.domain.exception.EntidadeNaoEncontradaException;
import com.algaworks.algafood.domain.exception.NegocioException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
	
	public static final String MSG_ERRO_GENERICA_USUARIO_FINAL
		= "Ocorreu um erro interno inesperado no sistema. "
	            + "Tente novamente e se o problema persistir, entre em contato com o administrador do sistema.";
	
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	        HttpHeaders headers, HttpStatus status, WebRequest request) {

	    ProblemType problemType = ProblemType.DADOS_INVALIDOS;
	    String detail = "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.";
	    
	    BindingResult bindingResult = ex.getBindingResult();
	    
	    List<Problema.Field> problemaFields = bindingResult.getFieldErrors().stream()
	    		.map(fieldError -> Problema.Field.builder()
	    				.name(fieldError.getField())
	    				.userMessage(fieldError.getDefaultMessage())
	    				.build())
	    		.collect(Collectors.toList());
	    
	    Problema problema = createProblemBuilder(status, problemType, detail)
	        .userMenssage(detail)
	        .fields(problemaFields)
	        .build();
	    
	    return handleExceptionInternal(ex, problema, headers, status, request);
	}         
	
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
		String detail = "Erro no corpo passado na requisição, Verifique a sintaxe";
		
		
		Problema problema = createProblemBuilder(status, problemType, detail)
				.userMenssage(detail)
		        .build();
		
		return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}
	
	private ResponseEntity<Object> handleInvalidFormatException(InvalidFormatException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request){
		
		String path = joinPath(ex.getPath());
		
		ProblemType problemType = ProblemType.CORPO_NAO_LEGIVEL;
		String datail = String.format("A propriedade '%s' recebeu o valor '%s',"
				+ "que é de um tipo inválido. Informa um valor capatível com o tipo '%s'", 
				path , ex.getValue(), ex.getTargetType().getSimpleName());
		
		Problema problema = createProblemBuilder(status, problemType, datail)
				.userMenssage(MSG_ERRO_GENERICA_USUARIO_FINAL)
				.build();
		
		return handleExceptionInternal(ex, problema, headers, status, request);
	}
	
	private ResponseEntity<Object> handlePropertyBindingException(PropertyBindingException ex,
	        HttpHeaders headers, HttpStatus status, WebRequest request) {
			
	    String path = joinPath(ex.getPath());
	    
	    ProblemType problemType = ProblemType.CORPO_NAO_LEGIVEL;
	    String detail = String.format("A propriedade '%s' não existe. "
	            + "Corrija ou remova essa propriedade e tente novamente.", path);

	    Problema problem = createProblemBuilder(status, problemType, detail)
	    		.userMenssage(MSG_ERRO_GENERICA_USUARIO_FINAL)
	    		.build();
	    
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
		ProblemType problemType = ProblemType.RECURSO_NAO_ENCONTRADA;
		String detail = e.getMessage();
		
		
		Problema problema = createProblemBuilder(status, problemType, detail)
				.userMenssage(detail)
		        .build();

		return handleExceptionInternal(e, problema, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(EntidadeEmUsoException.class)
	public ResponseEntity<?> tratarEntidadeEmUsoException(EntidadeEmUsoException e, WebRequest request) {
		HttpStatus status = HttpStatus.CONFLICT;
		ProblemType problemType = ProblemType.ENTIDADE_EM_USO;
		String datail = e.getMessage();
		
		Problema problema = createProblemBuilder(status, problemType, datail)
				.userMenssage(datail)
				.build();
				
		return handleExceptionInternal(e, problema, new HttpHeaders(), status, request);
	}
	
	@ExceptionHandler(NegocioException.class)
	public ResponseEntity<?> tratarNegocioException(NegocioException e, WebRequest request) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		ProblemType problemType = ProblemType.ERRO_NEGOCIO;
		String detail = e.getMessage();
		
		Problema problema = createProblemBuilder(status, problemType, detail)
				.userMenssage(detail)
		        .build();
		
		return handleExceptionInternal(e, problema, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleUncaught(Exception ex, WebRequest request) {
	    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;		
	    ProblemType problemType = ProblemType.ERRO_DE_SISTEMA;
	    String detail = MSG_ERRO_GENERICA_USUARIO_FINAL;

	    ex.printStackTrace();
	    
	    Problema problema = createProblemBuilder(status, problemType, detail)
	    		.userMenssage(detail)
	            .build();

	    return handleExceptionInternal(ex, problema, new HttpHeaders(), status, request);
	}                            
	
	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
	        HttpStatus status, WebRequest request) {
	    
	    if (body == null) {
	        body = Problema.builder()
	            .timestamp(LocalDateTime.now())
	            .title(status.getReasonPhrase())
	            .status(status.value())
	            .userMenssage(MSG_ERRO_GENERICA_USUARIO_FINAL)
	            .build();
	    } else if (body instanceof String) {
	        body = Problema.builder()
	            .timestamp(LocalDateTime.now())
	            .title((String) body)
	            .status(status.value())
	            .userMenssage(MSG_ERRO_GENERICA_USUARIO_FINAL)
	            .build();
	    }
	    
	    return super.handleExceptionInternal(ex, body, headers, status, request);
	}
	
	private Problema.ProblemaBuilder createProblemBuilder(HttpStatus status,
			ProblemType problemType, String detail) {

		    return Problema.builder()
		        .timestamp(LocalDateTime.now())
		        .status(status.value())
		        .type(problemType.getUri())
		        .title(problemType.getTitle())
		        .datail(detail);
		}
	
	@Override
	protected ResponseEntity<Object> handleTypeMismatch(org.springframework.beans.TypeMismatchException ex, HttpHeaders headers,
	        HttpStatus status, WebRequest request) {
	    
	    if (ex instanceof MethodArgumentTypeMismatchException) {
	        return handleMethodArgumentTypeMismatch(
	                (MethodArgumentTypeMismatchException) ex, headers, status, request);
	    }

	    return super.handleTypeMismatch(ex, headers, status, request);
	}

	private ResponseEntity<Object> handleMethodArgumentTypeMismatch(
	        MethodArgumentTypeMismatchException ex, HttpHeaders headers,
	        HttpStatus status, WebRequest request) {

	    ProblemType problemType = ProblemType.PARAMETRO_INVALIDO;

	    String detail = String.format("O parâmetro de URL '%s' recebeu o valor '%s', "
	            + "que é de um tipo inválido. Corrija e informe um valor compatível com o tipo %s.",
	            ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());

	    Problema problema = createProblemBuilder(status, problemType, detail)
	    		.userMenssage(detail)
	            .build();

	    return handleExceptionInternal(ex, problema, headers, status, request);
	}
	
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, 
	        HttpHeaders headers, HttpStatus status, WebRequest request) {
	    
	    ProblemType problemType = ProblemType.RECURSO_NAO_ENCONTRADA;
	    String detail = String.format("O recurso %s, que você tentou acessar, é inexistente.", 
	            ex.getRequestURL());
	    
	    Problema problema = createProblemBuilder(status, problemType, detail)
	    		.userMenssage(detail)
	            .build();
	    
	    return handleExceptionInternal(ex, problema, headers, status, request);
	}
	
}

package com.algaworks.algafood.api.exceptionhandler;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Builder;
import lombok.Getter;

@JsonInclude(Include.NON_NULL)
@Getter
@Builder
public class Problema {
	
	private Integer status;
	private String type;
	private String title;
	private String datail;
	private String userMenssage;
	private LocalDateTime timestamp;
	private java.util.List<Field> fields;
	
	@Getter
	@Builder
	public static class Field {
		
		private String name;
		private String userMessage;
	}
}

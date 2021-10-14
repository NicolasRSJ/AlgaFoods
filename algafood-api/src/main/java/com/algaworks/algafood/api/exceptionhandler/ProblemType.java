package com.algaworks.algafood.api.exceptionhandler;

import lombok.Getter;

@Getter
public enum ProblemType {
	
	ENTIDADE_NAO_ENCONTRADA("/entidade-nao-encontrada", "Entidade não Encontrada");
	 
	private String title;
	private String uri;
	
	ProblemType(String path, String title){
		this.uri = "https://algadoof.com.br" + path;
		this.title = title;
	}
	
}
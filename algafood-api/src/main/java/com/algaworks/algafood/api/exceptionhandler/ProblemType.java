package com.algaworks.algafood.api.exceptionhandler;

import lombok.Getter;

@Getter
public enum ProblemType {
	
	CORPO_NAO_LEGIVEL("/corpo-nao-legivel", "Não foi possivel ler o corpo"),
	ENTIDADE_NAO_ENCONTRADA("/entidade-nao-encontrada", "Entidade não Encontrada"),
	ENTIDADE_EM_USO("/entidade-em-uso", "Entidade em uso"),
	ERRO_NEGOCIO("/erro-negocio", "Erro de Negocio");
	 
	private String title;
	private String uri;
	
	ProblemType(String path, String title){
		this.uri = "https://algadoof.com.br" + path;
		this.title = title;
	}
	
}

package com.algaworks.algafood.api.exceptionhandler;

import lombok.Getter;

@Getter
public enum ProblemType {
	
	DADOS_INVALIDOS("/dados-invalidos", "Dados inválidos"),
	ERRO_DE_SISTEMA("/erro-de-sistema", "Erro de sistema"),
	PARAMETRO_INVALIDO("/parametro-invalido", "Parâmetro inválido"),
	CORPO_NAO_LEGIVEL("/corpo-nao-legivel", "Não foi possivel ler o corpo"),
	RECURSO_NAO_ENCONTRADA("/rescurso-nao-encontrada", "Recurso não Encontrado"),
	ENTIDADE_EM_USO("/entidade-em-uso", "Entidade em uso"),
	ERRO_NEGOCIO("/erro-negocio", "Erro de Negocio");
	 
	private String title;
	private String uri;
	
	ProblemType(String path, String title){
		this.uri = "https://algadoof.com.br" + path;
		this.title = title;
	}
	
}

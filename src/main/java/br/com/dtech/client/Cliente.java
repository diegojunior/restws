package br.com.dtech.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jettison.JettisonFeature;

import br.com.dtech.model.Cerveja;
import br.com.dtech.model.Cerveja.Tipo;
import br.com.dtech.model.rest.Cervejas;

public class Cliente {

	public static void main(String[] args) {
		Client client = ClientBuilder.newClient();
		//3 pilares do REST: URL, Tipo de Conteudo e Metodo HTTP
		Cervejas cervejas = client
			.target(Constants.HOST) //URL - aqui se informa URL base, para que possa se trocar de tipo de ambiente (DEV, PROD, HOMOLOG)
			.path("cervejas") //URL - aqui se informa o recurso
			.request(MediaType.APPLICATION_XML) //Tipo Conteudo - application/xml, application/json etc
			.get(Cervejas.class); //Metodo HTTP - GET, POST, PUT, DELETE etc
		
		List<Cerveja> cervejaList = new ArrayList<Cerveja>();
		
		for (Link link : cervejas.getLinks()) {
			Cerveja cerveja = ClientBuilder
				.newClient()
				.register(JettisonFeature.class)
				.invocation(link)
				.accept(MediaType.APPLICATION_JSON)
				.get(Cerveja.class);
			cervejaList.add(cerveja);
		}
		
		criarCerveja(new Cerveja("Skol1", "ruim", "AMBEV", Tipo.PILSEN));
	}
	
	private static Cerveja criarCerveja(Cerveja cervejaParam) {
		Cerveja cerveja = ClientBuilder
			.newClient()
			.target(Constants.HOST)
			.path("cervejas")
			.request(MediaType.APPLICATION_XML)
			//metodo POST tem como paramatro o tipo de conteudo e classe de retorno de conversao da resposta
			.post(Entity.entity(cervejaParam, MediaType.APPLICATION_XML), Cerveja.class);
		
		System.out.println("Inseriu a primeira: " + cerveja.getNome());
		
		cerveja.setNome(cervejaParam.getNome() + "-");
		// Aqui esta uma variação para se obter o tipo de retorno do Response,
		// para no caso se beneficiar de informaçoes do Location no cabecalho por exemplo.
		Response response = ClientBuilder
			.newClient()
			.target(Constants.HOST)
			.path("cervejas")
			.request()
			.post(Entity.xml(cervejaParam));
		
		System.out.println("Inseriu a segunda e vai exibir o location: " + response.getLocation());
		
		//Como o metodo POST esta retornando 201 com o Location, iremos pegar esse link
		Link link = Link
			.fromUri(response.getLocation())
			.build();
		
		Cerveja cervejaInserida = ClientBuilder
			.newClient()
			.invocation(link)
			.accept(MediaType.APPLICATION_XML)
			.get(Cerveja.class);
		
		System.out.println("Segunda cerveja com location: " + cervejaInserida.getNome());
		
		return cerveja;
		
	}
}

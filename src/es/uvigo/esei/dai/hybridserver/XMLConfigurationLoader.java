/**
 *  HybridServer
 *  Copyright (C) 2014 Miguel Reboiro-Jato
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.esei.dai.hybridserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;




public class XMLConfigurationLoader {
	public Configuration load(File xmlFile)
	throws Exception {
		/*Podemos validar aqui o hacerlo antes de entrar*/				
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		Document dom = builder.parse(xmlFile);
		dom.normalize();
		

		NodeList ppal = dom.getElementsByTagName("configuration");
		Node configurationNode = ppal.item(0);
		NodeList items = configurationNode.getChildNodes();
		
		int numPartsConfiguration=0;
		/*Recorremos los nodos para acceder a sus valores.*/
			for(int i=0 ; i<items.getLength(); i++){
				/*Comprobamos esto porque tambien nos devuelve como nodo #text*/
				if(items.item(i).getNodeType() == Node.ELEMENT_NODE){
					switch(items.item(i).getNodeName()){
					case "connections": loadConnections(items.item(i));
										numPartsConfiguration++;
										break;
					case "database":	loadDatabase(items.item(i));
										numPartsConfiguration++;
										break;
					case "servers": 	loadServers(items.item(i));
										numPartsConfiguration++;
										break;
					}
				
			}//if
		}//for
		
		if(numPartsConfiguration == 3){
			Configuration config = new Configuration(this.httpPort, this.numClients,
														this.webServiceURL, this.dbUser, 
															this.dbPassword, this.dbURL, 
																this.servers);
			return config;
		
		}else throw new Exception();
		/*a partir de un xml debe generar un configuration*/
		
	}
	
	/*Se encarga de la parte de parametros del server*/
	private void loadConnections(Node node) throws Exception{
		int numParameterConnection=0;
		NodeList connectionParameters= node.getChildNodes();
		for(int i = 0; i<connectionParameters.getLength(); i++){
			Node parameter= connectionParameters.item(i);
				if(parameter.getNodeType() == Node.ELEMENT_NODE){
					switch(parameter.getNodeName()){
					case "http": this.httpPort= Integer.parseInt(parameter.getTextContent());
								numParameterConnection++; 
								break;
					
					case "webservice": this.webServiceURL=parameter.getTextContent();
										numParameterConnection++;
										break;
					
					case "numClients": this.numClients=Integer.parseInt(parameter.getTextContent());
										numParameterConnection++;
										break;
					}
				}
		}
		if(numParameterConnection != 3) throw new Exception();
	}
	
	/*Se encarga de obtener parametros de la base de datos*/
	private void loadDatabase(Node node) throws Exception{
		int numParametersDB = 0;
		NodeList databaseParameters = node.getChildNodes();
		for(int i = 0; i<databaseParameters.getLength(); i++){
			Node parameter = databaseParameters.item(i);
				if(parameter.getNodeType() == Node.ELEMENT_NODE){
					switch(parameter.getNodeName()){
					case "user": this.dbUser= parameter.getTextContent();
								 numParametersDB++;
								 break;
					
					case "password": this.dbPassword= parameter.getTextContent();
									numParametersDB++;
									break;
					
					case "url": this.dbURL= parameter.getTextContent();
								numParametersDB++;
								break;
					}
				}
		}
		if(numParametersDB != 3) throw new Exception();
	}
	
	/*Se encarga de cargar los servers para P2P*/
	private void loadServers(Node node) throws Exception{
		int numParametersPerServer=0;
		this.servers = new ArrayList<ServerConfiguration>();
		NodeList serversParameters = node.getChildNodes();
		for(int i = 0; i<serversParameters.getLength(); i++){
			Node server = serversParameters.item(i);
			if(server.getNodeType() == Node.ELEMENT_NODE){
				NamedNodeMap attributes = server.getAttributes();
				ServerConfiguration serverConfiguration = new ServerConfiguration();
				
				for(int j = 0; j<attributes.getLength() ; j++){
					Node attribute = attributes.item(j);
					if( attribute.getNodeType() == Node.ATTRIBUTE_NODE ){
						
						switch(attribute.getNodeName()){
							case "name": 	serverConfiguration.setName(attribute.getNodeValue());
											numParametersPerServer++;
										 	break;
										 
							case "wsdl": 	serverConfiguration.setWsdl(attribute.getNodeValue());
											numParametersPerServer++;
										 	break;
										 
							case "namespace": 	serverConfiguration.setNamespace(attribute.getNodeValue());
												numParametersPerServer++;
												break;
											  
							case "service": 	serverConfiguration.setService(attribute.getNodeValue());
												numParametersPerServer++;
												break;
											
							case "httpAddress": serverConfiguration.setHttpAddress(attribute.getNodeValue());
												numParametersPerServer++;
												break;
												
							/*Deberia de controlar aqui si no es ninguna y soltar una excepcion??
							 * El documento viene validad asi que no deberia de ser necesario*/
						}//fin switch
					}//fin if
				}//fin for(j)
				
				//Ya tenemos todos los valores del serverConfiguration podemos meterlo en la lista.
				if(numParametersPerServer==5){
					this.servers.add(serverConfiguration);
					numParametersPerServer=0;
				}else throw new Exception();
			}
		}
	}
	
	
	/*Almacenamos los valores para crear un Configuration.
	 * Podriamos crear uno sin parametros y settear pero creo que 
	 * son muchas más operaciones*/
	private int httpPort;
	private int numClients;
	private String webServiceURL;
	
	private String dbUser;
	private String dbPassword;
	private String dbURL;
	
	private List<ServerConfiguration> servers;
	
}

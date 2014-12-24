package es.uvigo.esei.dai.hybridserver.http;


import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import es.uvigo.esei.dai.hybridserver.HybridService;
import es.uvigo.esei.dai.hybridserver.ServerConfiguration;
import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponseStatus;


import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;


public class Router {
	
	/*Aqui arreglar el dao, como tenemos varios
	 * hay que ver que mandamos, si la conexion 
	 * o como lo hacemos*/ 
	public HTTPResponse process(HTTPRequest httpRequest, Dao d, List<ServerConfiguration> services, int port){
		this.dao=d;
		this.response= new HTTPResponse();
		this.request= httpRequest;
		this.serverport=port;
		this.listServices=services;
		try{
			//En caso de que el httpRequest venga null el parser a fallado asi que soltamos S400
			if(httpRequest==null){
				setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S400);
				setResponseParameters();
				this.dao.closeConnection();
				return this.response;
			}else{
						 	
						switch(this.request.getMethod()){
						case GET: buildResponseGET();
								  break;
								 
											
						case POST: buildResponsePOST();
								   break;
								
						case DELETE:buildResponseDELETE();
									break;
									 
						
						default: setStatusLineToInternalError();
								 break;
						
						}
						this.dao.closeConnection();
						return this.response;
						
					}//else	
		//En caso de Exception soltamos un error S500 con setStatusLineToInternalError y parametros por defecto.
		}catch(Exception e){
			e.printStackTrace();
			setStatusLineToInternalError();
			setResponseParameters();
			return this.response;
		}
	}
	


	private void buildResponseGET() throws SQLException{
		this.rname=this.request.getResourceName();					
					/*Ahora dependiendo del tipo de recurso
					 * que nos manden vamos a crear un recurso
					 * de dicho tipo, en caso de default creamos tipo 
					 * html para devolver el hello!!
					 * 
					 * Hay que ver si en este momento inicializamos el dao????
					 * 
					 */
					switch(this.request.getResourceName()){
					case "html":	generateGetResponse(this.rname);
							  		break;
					/*En este hay que ver como hacemos
					 * para validar el xsd antes.
					 */
					case "xml": 	generateGetXResponse(this.rname);
									break;
					
					case "xsd": 	generateGetResponse(this.rname);
							   		break;
					
					case "xslt": 	generateGetResponse(this.rname);
							   		break;
					
					case "": 		generateHello();
									break;
					
					default: 		generateBadRequest();
									break;
					
				}
				
	}

	
	private void generateGetResponse(String type) throws SQLException{
		if(this.request.getResourceParameters().containsKey("uuid")){
			//Si contiene el uuid devolvemos la solicitada
			Map<String,String> parameters= this.request.getResourceParameters();
			String uuid=parameters.get("uuid");
			
			try{	
				if((this.resource=dao.getResource(type,uuid))!=null){
				setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
				setResponseParameters();
				setResponseContent(this.resource.getContent());
				}else if(this.listServices!=null){
					if((this.resource=findRemoteResource(type,uuid))!=null){
						setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
						setResponseParameters();
						setResponseContent(this.resource.getContent());
					}else{
						generateNotFound();
					}
				}else{
					generateNotFound();
				}
				
			}catch(SQLException | IllegalArgumentException e){
				setResponseStatusLine(HTTPHeaders.HTTP_1_1,	HTTPResponseStatus.S404);
				setResponseParameters();
			}
		}else{
			String locallinks = createDefaultListContent(type);
			String remotelinks="";
			if(this.listServices!=null){
			remotelinks += findAllRemoteResource(type);
			}
			this.resource=new HTML("default", locallinks+remotelinks);
			setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
			setResponseParameters();
			setResponseContent(this.resource.getContent());
		}
	}
	
	
	
	private Resource findRemoteResource(String type, String uuid) {
		List<HybridService> services = setServers();
		Resource resource= null;
		switch(type){
		case "html":for(HybridService hs: services){
					resource = hs.getHTMLResource(uuid);
						if(resource.getUUID()!=null)return resource;
					}
					break;
			
		case "xml":for(HybridService hs: services){
					resource = hs.getXMLResource(uuid);
						if(resource.getUUID()!=null)return resource;
					}
					break;
			
		case "xsd":for(HybridService hs: services){
					resource = hs.getXSDResource(uuid);
						if(resource.getUUID()!=null)return resource;
					}
					break;
		
		case "xslt":for(HybridService hs: services){
					resource = hs.getXSLTResource(uuid);
						if(resource.getUUID()!=null)return resource;
					}
					break;
		}
		return null;
	}
	
	private boolean deleteRemoteResource(String type, String uuid){
		List<HybridService> services = setServers();
		switch(type){
		case "html":for(HybridService hs: services){
					if(hs.removeRestourceOfType(type, uuid))return true;
					}
					break;
			
		case "xml":for(HybridService hs: services){
					if(hs.removeRestourceOfType(type, uuid))return true;
					}
					break;
			
		case "xsd":for(HybridService hs: services){
					if(hs.removeRestourceOfType(type, uuid))return true;
					}
					break;
		
		case "xslt":for(HybridService hs: services){
					if(hs.removeRestourceOfType(type, uuid))return true;
					}
					break;
		}
		return false;
	}
	
	
	private String findAllRemoteResource(String type) {
		List<HybridService> services = setServers();
		String remoteLinksResources="";
		int serverselected=0;
		switch(type){
		case "html":for(HybridService hs: services){
					System.out.println("paso por aki");
					//podemos hacer esto porque se crean en el mismo orden
					remoteLinksResources+="<h1>"+this.listServices.get(serverselected).getName()+"</h1>";
					serverselected++;
					remoteLinksResources += hs.getLinksAllResources(type);
					}
					break;
			
		case "xml":for(HybridService hs: services){
					remoteLinksResources+="<h1>"+this.listServices.get(serverselected).getName()+"</h1>";
					serverselected++;
					remoteLinksResources += hs.getLinksAllResources(type);
					}
					break;
			
		case "xsd":for(HybridService hs: services){
					remoteLinksResources+="<h1>"+this.listServices.get(serverselected).getName()+"</h1>";
					serverselected++;
					remoteLinksResources += hs.getLinksAllResources(type);
					}
					break;
		
		case "xslt":for(HybridService hs: services){
					remoteLinksResources+="<h1>"+this.listServices.get(serverselected).getName()+"</h1>/n";
					serverselected++;
					remoteLinksResources += hs.getLinksAllResources(type);
					}
					break;
		}
		return remoteLinksResources;
	}

	private List<HybridService> setServers(){
		List<HybridService> ss= new ArrayList<HybridService>();
			for(ServerConfiguration sc : this.listServices){
				URL url;
				try {
					url = new URL(sc.getWsdl());
					QName name = new QName( sc.getNamespace(),
							sc.getService());
					Service service = Service.create(url, name);
					ss.add(service.getPort(HybridService.class));
				} catch (Exception e) {
					e.printStackTrace();
					
				}
			}
			return ss;
	}
	
	private void generateGetXResponse(String type) throws SQLException{
		try{
			Resource xml,xsd,xslt = null;
			if(this.request.getResourceParameters().containsKey("uuid")){//sino devuelvo listado
				String xmluuid = this.request.getResourceParameters().get("uuid");
				if((xml=dao.getResource("xml", xmluuid))==null){
					if((xml=findRemoteResource("xml", xmluuid))==null) generateNotFound();
				}
				//Compruebo si solicitan xslt de transformacion y lo busco. Si no lo hay devuelvo 404 
				if(this.request.getResourceParameters().containsKey("xslt")){
				String xsltuuid= this.request.getResourceParameters().get("xslt");
					if((xslt=dao.getResource("xslt", xsltuuid))==null){
						if((xslt=findRemoteResource("xslt", xsltuuid))==null) generateNotFound();
						
					}

					//He encontrado el xml y el xslt por lo que es seguro que hay un xsd
					if((xsd=dao.getResource("xsd", xslt.getAuxResource()))==null){
						xsd=findRemoteResource("xsd", xslt.getAuxResource()); 
					}
					
					//valido el xml con el xsd. Si no es correcta devolvemos 400
					if(validationXMLWithXSD(xml,xsd)){
			   	   		   //Todo ok, transformamos y devolvemos.
						   this.resource=transformation(xml,xslt);
						   setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
						   setResponseParameters();
						   setResponseContent(this.resource.getContent());
					}else{
						generateBadRequest();
					}
					
					
				}else{
					//No contiene xslt por lo que devolvemos el xml tal cual
					this.resource=xml;
					setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
					setResponseParameters();
					setResponseContent(this.resource.getContent());
				}
					
				
				
			}else{
				//Listado del tipo de recurso solicitado en un html con enlaces
				String locallinks= createDefaultListContent(type);
				String remotelinks="";
				if(this.listServices!=null){
				remotelinks += findAllRemoteResource(type);
				}				
				this.resource=new HTML("default", locallinks+remotelinks);
				setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
				setResponseParameters();
				
				setResponseContent(this.resource.getContent());
			}
			
			
		}catch(Exception e){
			
			e.printStackTrace();
			generateNotFound();
		}
	}


//	private void generateGetXResponse(String type) throws SQLException{
//		if(this.request.getResourceParameters().containsKey("uuid")){
//			//Si contiene el uuid devolvemos la solicitada
//			Map<String,String> parameters= this.request.getResourceParameters();
//			String uuid=parameters.get("uuid");
//			
//			try{
//				Resource xml,xslt;
//				
//				if((xml=dao.getResource(type,uuid))==null){
//						setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S404);	
//					}else{
//						if(parameters.containsKey("xslt")){
//						   if((xslt=dao.getResource("xslt",	parameters.get("xslt")))!=null){
//							   
//						   	   	Resource xsd=dao.getResource("xsd", xslt.getAuxResource());
//						   	   	if(validationXMLWithXSD(xml,xsd)){
//						   	   		   //Todo ok, transformamos y devolvemos.
//									   this.resource=transformation(xml,xslt);
//									   setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
//									   setResponseParameters();
//									   setResponseContent(this.resource.getContent());
//									   
//								   }else{
//									   //No valida el xsd
//									   generateBadRequest();
//								   }
//							   
//						   }else{
//							   //No se encuentra el xslt solicitado
//							   generateNotFound();
//						   }
//						}else{
//								//No contiene xslt por lo que devolvemos el xml tal cual
//								this.resource=xml;
//								setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
//								setResponseParameters();
//								setResponseContent(this.resource.getContent());
//						}
//				}
//				
//			}catch(SQLException | IllegalArgumentException e){
//				setResponseStatusLine(HTTPHeaders.HTTP_1_1,	HTTPResponseStatus.S404);
//				setResponseParameters();
//			}
//		}else{
//			//Listado del tipo de recurso solicitado en un html con enlaces
//			this.resource=new HTML("default", createDefaultListContent(type));
//			setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
//			setResponseParameters();
//			setResponseContent(this.resource.getContent());
//		}
//	}
	
	
	private Resource transformation(Resource xml, Resource xslt) {
		try{
			/*Creo Document con xml*/
			StringReader xmlreader = new StringReader(xml.getContent());
			StringReader xsltreader = new StringReader(xslt.getContent());
			StringWriter writer = new StringWriter();
			/*Creacion y configuracion del transformador*/
			TransformerFactory factory = TransformerFactory.newInstance();
					
			Transformer transformer = factory.newTransformer(
					new StreamSource(xsltreader));
			
			transformer.transform(
					new StreamSource(xmlreader),
					new StreamResult(writer));
			Resource html= new HTML(xml.getUUID(), writer.toString());
			return html;
		}catch(Exception e){
			return null;
		}
	}



	private boolean validationXMLWithXSD(Resource xml, Resource xsd) {
		try {
			StringReader xmlReader = new StringReader(xml.getContent());
			StringReader xsdReader = new StringReader(xsd.getContent());
            SchemaFactory factory = 
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(xsdReader));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlReader));
        } catch (IOException | SAXException e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
	}



	private void buildResponsePOST() throws SQLException {
		Map<String, String> parameters =request.getResourceParameters();
		this.rname=this.request.getResourceName();
		

			switch(this.rname){
			case "html":generatePostHResponse(this.rname,parameters);
					  	break;
			case "xml": generatePostXResponse(this.rname,parameters);
					   	break;
			
			case "xsd": generatePostXResponse(this.rname,parameters);
					   	break;
			
			case "xslt":generatePostXResponse(this.rname,parameters); 
					   break;
			
			case "": generateBadRequest();
						break;
			
			
			default: 	generateBadRequest();
						break;
			
		}
			
	}
	
private void generatePostHResponse(String type, Map<String, String> parameters) throws SQLException {
		if(parameters.containsKey(this.request.getResourceName())){
		this.resource=dao.insertResources(type, parameters.get(this.request.getResourceName()));
		createPostContent();
		setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
		setResponseParameters();
		}
		else{
			generateBadRequest();
		}
	}
	
	
private void generatePostXResponse(String type, Map<String, String> parameters)throws SQLException {
	
	if(parameters.containsKey(this.request.getResourceName())){
	 	System.out.println(type+" "+parameters.get("xslt")+" "+parameters.containsKey("xsd"));
	 		if(parameters.containsKey("xslt")){
	 			if(parameters.containsKey("xsd")){
	 				if(this.dao.getResource("xsd",parameters.get("xsd"))!=null){
	 					System.out.println("Existe el recurso");
	 					this.resource=dao.insertXSLTResource(type,
								parameters.get("xslt"),
										parameters.get("xsd"));
		 				createPostContent();
						setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
						setResponseParameters();
						
	 				}else{
	 					System.out.println("BadRequest en catch");
	 					generateNotFound();
	 				}
	 				
	 			}else{
	 				generateBadRequest();
	 			}
	 		}else{
				System.out.println("Estoy aquiiiiiiiiiiii");
				/*Aqui en el dao hay que comprobar si parameters lleva algo
				 * aun no esta definido el metodo en los dao*/
				this.resource=dao.insertResources(type,parameters.get(this.request.getResourceName()));
				createPostContent();
				setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
				setResponseParameters();
				}
		
	}
	else{
		generateBadRequest();
	}
}



private void buildResponseDELETE() throws SQLException {
		Map<String,String> parameters = this.request.getResourceParameters();
		this.rname=this.request.getResourceName();
		

			switch(this.rname){
			case "html":	generateDeleteHResponse(this.rname,parameters);
							break;
			case "xml": 	generateDeleteXResponse(this.rname, parameters);
					   		break;
			
			case "xsd": 	generateDeleteXResponse(this.rname, parameters);
					   		break;
			
			case "xslt": 	generateDeleteXResponse(this.rname, parameters);
					   		break;
			
			case "": 	generateNotFound();
						break;
			
			
			default:	generateBadRequest();
						break;
			
		}

	}

	private void generateDeleteHResponse(String type, Map<String, String> parameters) throws SQLException {
		 String uuid="";
		 if((uuid=parameters.get("uuid"))!=null){
			  if((this.resource=this.dao.getResource(type,uuid))!=null){				  
				  this.dao.deleteResource(type, uuid);
				  setHtmlContentToDeleted(this.resource);
				  setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
				  setResponseParameters();
				 
			  }else{
				  if(this.listServices!=null){
					  if((this.resource=findRemoteResource(type, uuid))!=null){				  
						  deleteRemoteResource(type, uuid);
						  setHtmlContentToDeleted(this.resource);
						  setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
						  setResponseParameters();
					  }else{
					  generateNotFound();
					  }
				  }
			  }  
		  }else{
			  generateNotFound();
		  }
	
	}
					
				
	private void generateDeleteXResponse(String type, Map<String, String> parameters) throws SQLException{
		 String uuid="";
		 Resource xresource;
		 if((uuid=parameters.get("uuid"))!=null){
			  if((xresource=this.dao.getResource(type, uuid))!=null){
				  this.resource= new HTML(xresource.getUUID(), "");
				  this.dao.deleteResource(type, uuid);
				  setHtmlContentToDeleted(this.resource);
				  setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
				  setResponseParameters();
				 
			  }else{
				  if(this.listServices!=null){
					  if((xresource=findRemoteResource(type, uuid))!=null){
						  this.resource=new HTML(xresource.getUUID(),"");
						  deleteRemoteResource(type, uuid);
						  setHtmlContentToDeleted(this.resource);
						  setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
						  setResponseParameters();
					  }else{
					  generateNotFound();
					  }
				  }
			  }  
		  }else{
			  generateNotFound();
		  }
	
}



	private void generateNotFound() {
		setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S404);
		setResponseParameters();
}



	private void generateHello(){
		System.out.println("Multiple welcom");
		this.resource=new HTML("welcome", getWelcomeContent());
		 setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S200);
		 setResponseParameters();
		 setResponseContent(this.resource.getContent()); 
		 System.out.println(this.resource.getContent());
	}
	
	
	public void generateBadRequest(){
		this.resource=null;
		setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S400);
		setResponseParameters();
	}
				
	
///listado con todas las paginas que tenemos.
	private String createDefaultListContent(String type) throws SQLException {
		String content="";
		content+="<html>\n";
		content+="<body>\n";
		content+="<h1>Welcome to Hybrid Server</h1>\n";
		content+="<ul>\n";
			for(Resource rs : this.dao.getAllResources(type)){
				content+="<li>";
				content+="<a href=http://localhost:"+ 
					this.serverport+
					"/html?uuid="+rs.getUUID()+
					">"+rs.getUUID()+
					"</a>";
				content+="</li>\n";
			}
		content+="</ul>\n";
		content+="</body>\n";
		content+="</html>\n";
		return content;
	}
	
	

	private void createPostContent() {
		String body="<html>\n";
	   	body+="<body>\n";
	 	body+="<a href=\""+this.request.getResourceName()+"?uuid="+this.resource.getUUID()+
				"\">"+this.resource.getUUID()+
				"</a>\n";
	 	body+="</body>\n";
	 	body+="</html>\n";
	 	this.resource.setContent(body);
	 	this.response.setContent(body);
	}


		/// 	SETTERS   //// 

	private void setStatusLineToInternalError() {
		setResponseStatusLine(HTTPHeaders.HTTP_1_1, HTTPResponseStatus.S500);
		
	}


	private void setHtmlContentToDeleted(Resource rs) {
		String body="<html>\n";
	   	body+="<body>\n";
	 	body+="<h1>";
	   	body+= rs.getUUID();
	   	body+="  DELETED CORRECTLY";
	   	body+="</h1>\n";
	 	body+="</body>\n";
	 	body+="</html>\n";
	 	rs.setContent(body);
	 	this.response.setContent(body);
		
	}


	private void setResponseContent(String content) {
		this.response.setContent(content);
		
	}


	private void setResponseParameters() {
		this.response.putParameter("Server", "Hybrid Server");
		this.response.putParameter("Connection", "closed");
			//En caso de que el uuid no exista el html esta a null
			if(resource!=null){
				this.response.putParameter("Content-Type", resource.getType());
		  		this.response.putParameter("Content-Length", String.valueOf(resource.getLength()));
			}
	}


	private void setResponseStatusLine(HTTPHeaders header, HTTPResponseStatus status) {
		this.response.setVersion(header.getHeader());
  		this.response.setStatus(status);
  		
	}
	
		/// Welcome Content ///

	private String getWelcomeContent() {
		String content="";
		content+="<html>\n";
		content+="<body>\n";
		content+="<h1>Welcome to Hybrid Server</h1>\n";
		content+="<p>Created by:<p>\n";
		content+="<ul>\n<li>Jose Juan Dios Otero</li>\n";
		content+="</body>\n";
		content+="</html>\n";
		return content;
	}


	private HTTPResponse response;
	private HTTPRequest request;
	private String rname;
	private Resource resource=null;
	private Dao dao;
	private int serverport;
	private List<ServerConfiguration> listServices;
	private List<String> listActiveService;

}

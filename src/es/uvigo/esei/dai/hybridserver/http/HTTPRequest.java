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
package es.uvigo.esei.dai.hybridserver.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;



public class HTTPRequest {
	
	public HTTPRequest(Reader reader) throws IOException, HTTPParseException {
		BufferedReader bfr= new BufferedReader(reader);
		
		//Comprobamos la primera linea para decidir tipo de parser y si esta correcta
		//Si no esta correcta dejamos subir una excepcion HTTPParse que captura el ServiceThread generando un S400
		setHeader(bfr.readLine());
			
		
			
			switch(this.method){
			case GET: 	parserGet(bfr);
					  	break;
			
			case POST: 	parserPost(bfr);
					   	break;
			//Este caso no hace nada por que una peticion delete solo tiene una linea y ya esta parseada
			//en setHeader
			case DELETE:	break;
			
			//En caso de fallo soltamos una excepcion.
			default: throw new HTTPParseException();
						  
			
			}
			
	}
	
	
	
	
	//Leemos una linea que vendra compuesta por nombre: valor y hacemos putParameter que la gestiona.
	private void parserGet(BufferedReader bfr) throws IOException, HTTPParseException {
		String part=" ";
		while(!(part=bfr.readLine()).equals("")){
			putParameter(part);
		}
		
	}
	
	private void parserPost(BufferedReader bfr) throws IOException, HTTPParseException {
		String part=" ";
		
		
//		Parseo los parametros de cabecera e inserto
		while(!(part=bfr.readLine()).equals("")){
			putParameter(part);
		}
				//Si existe una cabecera que se llame content length miro si es mayor que 0
				//Si mayor que cero leo el cuerpo e inserto parametros.
				if(this.headerParameters.containsKey("Content-Length")){
					setContentLengt();
					
						if(this.contentLength!=0){
							char[] body= new char[this.contentLength];
							bfr.read(body, 0, this.contentLength);
							setContent(body);
							setResourceParameters(getContent());
						}
				}
	
	}
	

	private void putParameter(String part) throws HTTPParseException {
	
		if(part.contains(":")){
				String[] varAndVal= part.split(":");
				
				this.headerParameters.put(varAndVal[0], varAndVal[1].replaceAll(" ", ""));
		
		}
		else{
			throw new HTTPParseException("Invalid Header");
		}
		
	}
	

	
		////		SETERS			////
	
private void setHeader(String firstLine) throws HTTPParseException {
		System.out.println("Seteando cabecera...");
		System.out.println(firstLine);
		if(firstLine!=null){
		if(firstLine.contains(" ")){	
			String[] parts= firstLine.split(" ");
			
			if(parts.length==3){
								
					setMethod(parts[0]);
				
							
					setResourcePathAndParameters(parts[1]);
					setResourceChain(parts[1]);
					
					setVersion(parts[2]);
								
			}else
				{
					throw new HTTPParseException("Invalid Header");	
				}	
		}
		else{
			throw new HTTPParseException("Invalid Header");
		}
		}else{
			throw new HTTPParseException("null header");
		}
	}
	
	
	private void setContentLengt() {
		int length=Integer.parseInt(
				this.headerParameters.get(
						HTTPHeaders.CONTENT_LENGTH.getHeader()));
	
		this.contentLength=length;
		
	}


	private void setResourceChain(String chain) {
	
		this.resourceChain=chain;
	}

	
	
	
	private void setMethod(String meth) throws HTTPParseException {
			this.method= HTTPRequestMethod.valueOf(meth);
	}
	
		
	//Esto suele venir junto y separo por ? para parsear path y parameters
	private void setResourcePathAndParameters(String pAP) throws HTTPParseException{
			
		if(pAP.contains("?")){
				String[] parts=pAP.split("\\?");
				setResourcePath(parts[0]);	
				setResourceParameters(parts[1]);
				
			}
			else{
				setResourcePath(pAP);
			}
		
	}

	
	private void setResourceParameters(String parameters) throws HTTPParseException{
	
		if(parameters.contains("&")){	
			
			String[] parts = parameters.split("&");
			for(int i=0; i < parts.length; i++){
				
				if(parts[i].contains("=")){
					String[] varAndVal= parts[i].split("=");
					this.resourceParameters.put(varAndVal[0], varAndVal[1]);						
				}
				else{
					//Si no contiene el = se entiende que viene vacio el parametro.
					this.resourceParameters.put(parts[i], "");
				}
				
			}
		
		}
		else{
			if(parameters.contains("=")){
				String[] parts= parameters.split("=");
				this.resourceParameters.put(parts[0], parts[1]);
			}
			else throw new HTTPParseException("Invalid Parameter, whitout value");
			
		}
		
	}
	

	private void setResourcePath(String path) throws HTTPParseException {
		
		if(!path.startsWith("/")){
			throw new HTTPParseException("Invalid Paht");
		}
		if(!path.equals("/")){	
			
				String[] ppath= path.split("/");
				String[] aux = new String[ppath.length-1];
					//Esto lo hago porque el split genera un el ppath[0] como espacio en blanco ya que el path
					//empieza por /
					for(int i=0; i <aux.length; i++){
						aux[i]=ppath[i+1];
					}
					this.resourcePath=aux;
					setResourceName(path.substring(1));
					
		}
				
	}

	
	private void setResourceName(String name) {
		this.resourceName=name;		
	}
	
	
	private void setVersion(String version) throws HTTPParseException{
		try{
			if(HTTPHeaders.HTTP_1_1.getHeader().equalsIgnoreCase(version)){
			this.httpVersion= version;
			}
		//En caso de que lo anterior sea invalido cambio la excepcion por httpparseexcepcion	
		}catch(IllegalArgumentException e){
			throw new HTTPParseException("Invalid Version");
		}
		
	}
	

	private void setContent(char[] body) throws HTTPParseException {
		String type = this.headerParameters.get("Content-Type");
		
		if (type != null && type.startsWith("application/x-www-form-urlencoded")) {
		   try {
			this.content = URLDecoder.decode(String.valueOf(body), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new HTTPParseException();
		}
		}
		else{
			this.content=String.valueOf(body);
		}
		
	}
	

	////		GETERS		////

	public HTTPRequestMethod getMethod() {
		return this.method;
	}

	public String getResourceChain() {
		return this.resourceChain;
	}
	
	public String[] getResourcePath() {
		return this.resourcePath;
	}
	
	public String getResourceName() {
		
		return this.resourceName;
	}
	
	public Map<String, String> getResourceParameters() {
		return this.resourceParameters;
	}

	public String getHttpVersion() {
		return this.httpVersion;
	}

	public Map<String, String> getHeaderParameters() {
		return this.headerParameters;
	}

	public String getContent() {
		return this.content;
	}
	
	public int getContentLength() {
		return this.contentLength;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(this.getMethod().name())
			.append(' ').append(this.getResourceChain())
			.append(' ').append(this.getHttpVersion())
		.append('\n');
		
		for (Map.Entry<String, String> param : this.getHeaderParameters().entrySet()) {
			sb.append(param.getKey()).append(": ").append(param.getValue()).append('\n');
		}
		
		if (this.contentLength > 0) {
			sb.append('\n').append(this.getContent());
		}
		
		return sb.toString();
	}
	
	
	
	
	
	private String httpVersion="";
	private String content=null;
	private HTTPRequestMethod method=null;
	private int contentLength=0;
	private String[] resourcePath=null;
	private String resourceChain="";
	private String resourceName="";
	private Map<String, String> headerParameters= new LinkedHashMap<String, String>();
	private Map<String, String> resourceParameters= new HashMap<String, String>() ;
	
	
}

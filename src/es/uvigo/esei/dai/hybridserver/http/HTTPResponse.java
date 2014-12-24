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



import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPResponse {
	public HTTPResponse() {
	}
	
	public HTTPResponseStatus getStatus() {
		return this.status;
	}

	public void setStatus(HTTPResponseStatus status) {
		this.status=status;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version=version;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content=content;
		this.headerParameters.put("Content-Length", String.valueOf(content.length()));
	}

	public Map<String, String> getParameters() {
		return this.headerParameters;
	}
	
	public String putParameter(String name, String value) {
		return this.headerParameters.put(name, value);
	}
	
	public boolean containsParameter(String name) {
		if(this.headerParameters.containsKey(name)){
			return true;
		}
		
		return false;
	}
	
	public String removeParameter(String name) {
		return this.headerParameters.remove(name);
	}
	
	public void clearParameters() {
		this.headerParameters.clear();
		
	}
	
	public List<String> listParameters() {
		List<String> list= new ArrayList<String>();
		for(String i : this.headerParameters.keySet()){
			list.add(i);
		}
		return list;
	}

	public void print(Writer writer) throws IOException {
		System.out.println("entra en print");
		this.response+= getVersion() + " " + 
					getStatus().getCode() +" " +
					getStatus().getStatus()+ "\n";
		for(String i : listParameters()){
			this.response+= i +": "+ this.headerParameters.get(i)+"\n";
		}
		
		response+="\n" + getContent();
		System.out.println("Contenido de respuesta: " + this.response + " |");
		writer.write(this.response);
		writer.flush();
	}
	
	@Override
	public String toString() {
		final StringWriter writer = new StringWriter();
		
		try {
			this.print(writer);
		} catch (IOException e) {}
		
		return writer.toString();
	}
	
	
	private String content="";
	private HTTPResponseStatus status;
	private String version="";
	private Map<String,String> headerParameters= new HashMap<String,String>();
	private String response="";
	
}

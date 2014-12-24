package es.uvigo.esei.dai.hybridserver.http;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class IMDAO implements Dao {
	private Map<String,Resource> pages;
	
	public IMDAO(Map<String, String> initialPages){
		pages= new HashMap<String, Resource>();
		for(String key : initialPages.keySet()){
			insertResource("html", key, initialPages.get(key));
		}
				
	}
	
	public IMDAO(){
		pages= new HashMap<String, Resource>();
		String body="";
              
        for(int i=0; i<5 ; i++){        
    		body+="<html>\n";
            body+="<body>\n";
            body+="<h1>Welcome to, Hybrid Server!" + "Pagina " + String.valueOf(i) + "</h1>\n";
            body+="</body>\n";
            body+="</html>\n";
        	UUID uuid= java.util.UUID.randomUUID();
        	System.out.println(uuid.toString());
        	insertResources(uuid.toString(), body);
        	body="";
        }     
		
	}
	
	@Override
	public void setConnection(Connection connection) {
		// nothing to do
		
	}
	

	@Override
	public HTML insertResource(String type, String uuid, String content) {
		this.pages.put(uuid, new HTML(uuid,content));
		return (HTML) this.pages.get(uuid);
	}

	@Override
	public HTML insertResources(String type, String content) {
		UUID uuid = java.util.UUID.randomUUID();
		HTML html = new HTML(uuid.toString(), content);
		this.pages.put(uuid.toString(), html);
		
		///Devuelvo un nuevo objeto porque al cambiar el content para la respuesta
		// no puedo referenciar el insertado. Esto no pasa con DBDAO
		return new HTML(uuid.toString(),content);
	}

	@Override
	public void deleteResource(String type, String uuid) {
		this.pages.remove(uuid);
		
	}

	@Override
	public void updateResource(String type, Resource resource) {
		this.pages.put(resource.getUUID(), resource);
		
	}

	@Override
	public Resource getResource(String type, String uuid) {
		return this.pages.get(uuid);
	}

	@Override
	public List<Resource> getAllResources(String type) {
		List<Resource> list= new ArrayList<Resource>();
		for(String uuid : this.pages.keySet()){
			list.add((HTML) this.pages.get(uuid));
		}
		return list;
	}

	@Override
	public void closeConnection() throws SQLException {
		//  Nothing to do	
	}

	
	/*Metodo implementado en el DBDAO no necesario aqui*/
	@Override
	public HTML insertXSLTResource(String type, String content,
			String xsd) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	



	
	

}

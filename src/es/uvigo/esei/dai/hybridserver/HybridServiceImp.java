package es.uvigo.esei.dai.hybridserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.jws.WebService;

import es.uvigo.esei.dai.hybridserver.http.HTML;
import es.uvigo.esei.dai.hybridserver.http.Resource;
import es.uvigo.esei.dai.hybridserver.http.XML;
import es.uvigo.esei.dai.hybridserver.http.XSD;
import es.uvigo.esei.dai.hybridserver.http.XSLT;
@WebService(
	serviceName="HybridServerService",
	endpointInterface="es.uvigo.esei.dai.hybridserver.HybridService",
	targetNamespace="http://hybridserver.dai.esei.uvigo.es/"
		)


public class HybridServiceImp implements HybridService {
	private Properties properties;
	private int serverPort;
	
	public HybridServiceImp(Properties p,int port){
		this.properties=p;
		this.serverPort=port;
	}
	
	
	private Connection connect(){
		
		Connection connection;
		try {
			connection = DriverManager.getConnection(this.properties.getProperty("db.url"),
												 	this.properties.getProperty("db.user"),
												 	this.properties.getProperty("db.password"));
			return connection;
		} catch (SQLException e) {
			
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public String getLinksAllResources(String type) {
		List<Resource> list=new ArrayList<Resource>();
		getAllResourcesOfType(type,list);
		String links = generateLinksResources(list);
		return links;
		
		
	}


	@Override
	public boolean removeRestourceOfType(String type, String uuid) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private String generateLinksResources(List<Resource> list) {
		String content="";
		content+="<ul>\n";
			for(Resource rs : list){
				content+="<li>";
				content+="<a href=http://localhost:"+ 
					this.serverPort+
					"/html?uuid="+rs.getUUID()+
					">"+rs.getUUID()+
					"</a>";
				content+="</li>\n";
			}
		content+="</ul>\n";
		return content;
	}
	
	private void getAllResourcesOfType(String type, List<Resource> lista) {
		Connection connection = connect();
		String sql="SELECT * FROM "+type.toUpperCase();
		
		try(PreparedStatement statement = connection.prepareStatement(sql)){
			try(ResultSet rs = statement.executeQuery()){ 
			
				
				    while(rs.next()){
					lista.add(new HTML(rs.getString(1),
							rs.getString(2)));
					}
				}
			  
			} catch (SQLException e) {
				
				e.printStackTrace();
				closeConnection(connection);
			}
		closeConnection(connection);
	}



	@Override
	public HTML getHTMLResource(String uuid) {
		UUID uid= UUID.fromString(uuid);
		
		Connection connection = connect();
		
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM HTML WHERE uuid=?")){
			statement.setString(1, uid.toString());
										
			ResultSet rs = statement.executeQuery();
					
					if(rs.next()){
						HTML html= new HTML(rs.getString(1), rs.getString(2));
						closeConnection(connection);
						return html;
		
					}else{
						closeConnection(connection);
						return new HTML(null,null);
					}
			} catch (SQLException e) {
				
				closeConnection(connection);
				e.printStackTrace();
				return new HTML(null,null);
			}
	}



	@Override
	public XML getXMLResource(String uuid) {
		UUID uid= UUID.fromString(uuid);
		Connection connection= connect();
		
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM XML WHERE uuid=?")){
			statement.setString(1, uid.toString());
										
			ResultSet rs = statement.executeQuery();
					
					if(rs.next()){
						XML xml = new XML(rs.getString(1),rs.getString(2));
						closeConnection(connection);	
						return xml;
		
					}else{
						closeConnection(connection);
						return new XML(null, null);
					}
			} catch (SQLException e) {
				closeConnection(connection);
				return new XML(null,null);
			}
	}



	@Override
	public XSD getXSDResource(String uuid) {
		Connection connection=connect();
		UUID uid= UUID.fromString(uuid);
		
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM XSD WHERE uuid=?")){
			statement.setString(1, uid.toString());
										
			ResultSet rs = statement.executeQuery();
					
					if(rs.next()){
						XSD xsd = new XSD(rs.getString(1),rs.getString(2));
						closeConnection(connection);
						return xsd;
		
					}else{
						closeConnection(connection);
						return new XSD(null, null);
					}
			} catch (SQLException e) {
				closeConnection(connection);
				return new XSD(null, null);
			}
	}



	@Override
	public XSLT getXSLTResource(String uuid) {
		
		UUID uid= UUID.fromString(uuid);
		Connection connection = connect();
		try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM XSLT WHERE uuid=?")){
			statement.setString(1, uid.toString());
										
			ResultSet rs = statement.executeQuery();
					
					if(rs.next()){
						
						XSLT xslt = new XSLT(rs.getString(1),rs.getString(2),rs.getString(3));
						closeConnection(connection);
						
						return xslt;
		
					}else{
						
						closeConnection(connection);
						return new XSLT(null, null, null);
					}
			} catch (SQLException e) {
				
				e.printStackTrace();
				closeConnection(connection);
				return new XSLT(null, null, null);
			}
	}
	
	private void closeConnection(Connection c){
		try {
			c.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

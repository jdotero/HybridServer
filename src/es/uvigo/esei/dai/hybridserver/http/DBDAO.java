package es.uvigo.esei.dai.hybridserver.http;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DBDAO implements Dao{
	
    public Connection connection;
   
   public DBDAO()
   {
	   
   }
    
   public DBDAO(Map<String, String> pages)
   {
	   for(String uuid : pages.keySet()){
		   HTML html= new HTML(uuid, pages.get(uuid));
		   insertResource("html",html.getUUID(),html.getContent());
	   }
   }

   public void setConnection(Connection c)
	   {
		  this.connection=c;
	   }
   
	@Override
	public HTML insertResource(String type, String uuid, String content) {
		String sql= "INSERT INTO"+type.toUpperCase()+"(uuid, content) ";
		sql+="VALUES (?,?)";
			
			try(PreparedStatement statement= 
					connection.prepareStatement(sql)){
				
				statement.setString(1, uuid.toString());
				statement.setString(2, content);
				
				statement.executeUpdate();
				return new HTML(uuid.toString(), content);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}
	
	@Override
	public HTML insertResources(String type, String content) {
		String sql= "INSERT INTO "+type.toUpperCase()+" (uuid, content) ";
		sql+="VALUES (?,?)";
			
			try(PreparedStatement statement= 
					connection.prepareStatement(sql)){
				
				
				UUID uuid= java.util.UUID.randomUUID();
				statement.setString(1, uuid.toString());
				statement.setString(2, content);
				
				statement.executeUpdate();
				return new HTML(uuid.toString(), content);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		
	}
	
	@Override
	public HTML insertXSLTResource(String type, String content, String xsd) throws SQLException{
		String sql= "INSERT INTO "+type.toUpperCase()+" (uuid, content, xsd) ";
		sql+="VALUES (?,?,?)";
			System.out.println(sql);
			try(PreparedStatement statement= 
					connection.prepareStatement(sql)){
				UUID uuid= java.util.UUID.randomUUID();
				statement.setString(1, uuid.toString());
				statement.setString(2, content);
				statement.setString(3, xsd);
				
				statement.executeUpdate();
				return new HTML(uuid.toString(), content);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
	}

	@Override
	public void deleteResource(String type, String uuid) throws SQLException {
		// TODO Auto-generated method stub
		String sql="";
		if(type.equals("xsd")){
			System.out.println("primero borro los xslt");
			 sql="DELETE FROM XSLT WHERE xsd=?";
			
			try(PreparedStatement statement
					=connection.prepareStatement(sql)){ 
				
			
				statement.setString(1, uuid);
				statement.executeUpdate();
			}catch(Exception e){
				throw new SQLException();
			}
		}
		
		sql="DELETE FROM "+ type.toUpperCase()
				+ " WHERE uuid=?";

		

		try(PreparedStatement statement
				=connection.prepareStatement(sql)){ 
			
		
			statement.setString(1, uuid);
			
			int result=statement.executeUpdate();
			if(result==0 | result>1) throw new SQLException();
			
		}catch(Exception e){
			throw new SQLException();
		}
	}

	@Override
	public void updateResource(String type, Resource resource) throws SQLException {
		String sql="UPDATE "+ type.toUpperCase()
				+ " SET uuid=?, content=?"
				+ "WHERE uuid=?";
		
		try(PreparedStatement statement= connection.prepareStatement(sql)){
			statement.setString(1, resource.getUUID());
			statement.setString(2, resource.getContent());
			statement.setString(3, resource.getUUID());
			
			int test=statement.executeUpdate();
			if(test>1){
				throw new SQLException();
			}
		}
				
		
	}

	

	@Override
	public List<Resource> getAllResources(String type) throws SQLException {
		List<Resource> lista=new ArrayList<Resource>();
		String sql="SELECT * FROM "+type.toUpperCase();
		
		try(PreparedStatement statement = connection.prepareStatement(sql)){
			try(ResultSet rs = statement.executeQuery()){ 
			
				
				    while(rs.next()){
					lista.add(new HTML(rs.getString(1),
							rs.getString(2)));
					}
				    return lista; 
				}
			  
			}

	}

	@Override
	public void closeConnection() throws SQLException {
		this.connection.close();
		
	}

	@Override
	public Resource getResource(String type, String uuid) throws SQLException{
	System.out.println("uuid de xslt: " + uuid);
	UUID uid= UUID.fromString(uuid);
	
	try(PreparedStatement statement = connection.prepareStatement("SELECT * FROM "+type.toUpperCase()+" WHERE uuid=?")){
		statement.setString(1, uid.toString());
									
			try(ResultSet rs = statement.executeQuery()){
				
				if(rs.next()){
						
					switch(type){
					case "html":return new HTML(rs.getString(1),
													rs.getString(2));
					case "xml": return new XML(rs.getString(1),
												rs.getString(2));
					case "xsd": return new XSD(rs.getString(1),
												rs.getString(2));
					case "xslt":return new XSLT(rs.getString(1),
													rs.getString(2),
														rs.getString(3));
					default: return null;
					}

				}else{
					return null;
				}
			}
		}
	}

	

}

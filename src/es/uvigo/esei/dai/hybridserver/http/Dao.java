package es.uvigo.esei.dai.hybridserver.http;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;



//Esto deberia ser un Dao<T> pero para ello deberia instanciarse cuando
// ya se sabe el tipo de dato a utilizar.
// De momento el dao se instancia en el HybridServer por lo que no sabemos
// que tipo de dao necesitaremos.
public interface Dao {
	public void setConnection(Connection connection);
	public HTML insertResources(String type, String content) throws SQLException;
	public HTML insertResource(String type, String uuid, String content) throws SQLException;
	public HTML insertXSLTResource(String type, String content, String xsd) throws SQLException;
	public void deleteResource(String type, String uuid) throws SQLException;
	public void updateResource(String type, Resource resource) throws SQLException;
	public Resource getResource(String type, String uuid) throws SQLException;
	public List<Resource> getAllResources(String type) throws SQLException;
	public void closeConnection()throws SQLException;
	

}

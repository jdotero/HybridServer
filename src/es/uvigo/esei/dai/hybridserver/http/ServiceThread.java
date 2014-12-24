package es.uvigo.esei.dai.hybridserver.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;



import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPResponse;
import es.uvigo.esei.dai.hybridserver.ServerConfiguration;

public class ServiceThread implements Runnable {
	private  Socket socket;
	private  Properties properties;
    private  Dao dao;
    private  List<ServerConfiguration> listServers;
    private  int port;
	
	public ServiceThread(Socket s, Properties p, Dao d, List<ServerConfiguration> services, int serverPort) {
		this.socket=s;
		this.properties=p;
		this.dao=d;
		this.listServers=services;
		this.port=serverPort;
		
		
			//Si el dao es de tipo DBDAO necesito una conexion sino no
			if(dao.getClass().equals(DBDAO.class)){
				this.dao.setConnection(doConnection());
			}
	}

	

	@Override
	public void run() {
		//Esta asignacion la hago para que el try con recursos me cierre el socket al final	
		try(Socket socket = this.socket){
			try{
			HTTPRequest request = new HTTPRequest(
										new InputStreamReader(
												socket.getInputStream()));
			
			System.out.println("Valor de request");
			System.out.println(request.toString());
			Router router = new Router();
			HTTPResponse response = router.process(request,dao,listServers,port);
			
			
			response.print(new  OutputStreamWriter(socket.getOutputStream()));
			System.out.println("Termina la peticion");
			}catch(Exception e){
				e.printStackTrace();
				
				try {
					Router errorRouter = new Router();
					//Recibe null el HTTPRequest y genera un response S400
					HTTPResponse errorResponse= errorRouter.process(null, this.dao,listServers,port);
					System.out.println("Enviando 400 de null request");
					errorResponse.print(new OutputStreamWriter(socket.getOutputStream()));
					System.out.println("Termina la peticion por null");
				} catch (IOException ioe) {
					System.out.println("Esta entrando aqui");
					ioe.printStackTrace();
				}
			}
		//En caso se genere un HTTPParseException o IOException se genere un error 400
		}
//		catch(HTTPParseException | IOException e){
//					e.printStackTrace();
//					Router errorRouter = new Router();
//					//Recibe null el HTTPRequest y genera un response S400
//					HTTPResponse errorResponse= errorRouter.process(null, this.dao);
//					try {
//						errorResponse.print(new OutputStreamWriter(socket.getOutputStream()));
//					} catch (IOException ioe) {
//						System.out.println("Esta entrando aqui");
//						ioe.printStackTrace();
//					}
//					
//					
//		}
 catch (IOException e1) {
	// TODO Auto-generated catch block
	e1.printStackTrace();
}
		
		
		
		
	}
	
	private Connection doConnection() {
		
		Connection connection;
		try {
			System.err.println(properties.getProperty("db.url"));
			System.err.println(properties.getProperty("db.user"));
			System.err.println(properties.getProperty("db.password"));
			connection = DriverManager.getConnection(properties.getProperty("db.url"),
												 	properties.getProperty("db.user"),
												 	properties.getProperty("db.password"));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("error den doConnection ServiceThread");
			e.printStackTrace();
			return null;
		}
			return connection;
		
	}
	
	
}

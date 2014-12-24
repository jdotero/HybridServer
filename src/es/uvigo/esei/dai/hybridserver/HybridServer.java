package es.uvigo.esei.dai.hybridserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import es.uvigo.esei.dai.hybridserver.http.DBDAO;
import es.uvigo.esei.dai.hybridserver.http.Dao;
import es.uvigo.esei.dai.hybridserver.http.IMDAO;
import es.uvigo.esei.dai.hybridserver.http.ServiceThread;


public class HybridServer {
	
	///	properties ///
	
	///Propiedades final porque no cambian en toda la ejecucion del server
	///Hay que apagar y encender con nuevas propiedades para cambiar.
	private Properties dbproperties;
	private Configuration config;
	private int server_port;
	private int num_clients;
	private List<ServerConfiguration> serviceServers;
	
	
	//Interfaz dao con vistas a la entrega dos.
	private final Dao dao;
	
	
	
	private Thread serverThread;
	private boolean stop;
	
	public HybridServer() {
		//Constructor por defecto y con base de datos en memoria.
		dbproperties=new Properties(setDeaultProperties());
		//el configuration queda a null
		this.config=null;
		this.dao= new IMDAO();
		serviceServers=null;
		
	}
		//Constructor con parametros de inicializacion y con base de datos en memoria, se podria usar tambien con DB
		//no lo hago por las pruebas
	public HybridServer(Map<String,String> pages){
		dbproperties= new Properties(setDeaultProperties());
		this.dao= new IMDAO(pages);
		this.config=null;
		serviceServers=null;
	}
	
		//Constructor con propiedades para la inicializacion del servidor.
	public HybridServer(Properties properts){
		this.config=null;
		dbproperties=properts;
		server_port=Integer.valueOf(dbproperties.getProperty("port"));
		num_clients=Integer.valueOf(dbproperties.getProperty("numClients"));
		dao= new DBDAO();
		serviceServers=null;
	
	}
	
	public HybridServer(Configuration configuration){
		//Guardamos la configuracion como una variable final
		this.config=configuration;
		
		
		/*Para no tocar el codigo anterior vamos a utilizar un objeto
		 * properties a partir del CONFIG*/
		
		dbproperties=setDBConfig();
		
		/*Dejo static el listado de servers para hacerlo accesible
		 * desde el router*/
		serviceServers = this.config.getServers();
		
		/*Guardamos configuraciones que necesitamos durante la ejecucion
		 * como static*/
		server_port=this.config.getHttpPort();
		num_clients=this.config.getNumClients();
		
		//En este caso tambien uso el dbdao	
		dao=new DBDAO();
		
		
	}
	
	
	
	private Properties setDeaultProperties() {
		Properties dbp=new Properties();
		
		//Seteamos a valores por defecto puerto y numero de clientes
		server_port=8888;
		num_clients=50;
		dbp.put("db.url", "jdbc:mysql://localhost/hybridserverdb");
		dbp.put("db.user","dai");
		dbp.put("db.password","daipassword");
		return dbp;
	}
	
	
	private Properties setDBConfig() {
		
		Properties dbproperts=new Properties();
		
		dbproperts.put("db.url", config.getDbURL());
		
		dbproperts.put("db.user", config.getDbUser());
		
		dbproperts.put("db.password", config.getDbPassword());
		return dbproperts;
	}
	
	
	//Metodos publicos
	public void start(){
		
		this.serverThread = new Thread(){
			@Override
			public void run(){
				
				if(config!=null){
					if(config.getWebServiceURL()!=null){
				Endpoint.publish(config.getWebServiceURL(), new HybridServiceImp(dbproperties,server_port));
					}
				}
				
				try(final ServerSocket ss= new ServerSocket(server_port)){
					
					ExecutorService poolService= Executors.newFixedThreadPool(num_clients);
					while(true){
						
							Socket clientSocket = ss.accept();
							
								if(stop) break;
								poolService.execute(new ServiceThread(clientSocket,
																		dbproperties,
																		dao,
																		serviceServers,
																		server_port));
								
							}
					
				}catch(IOException e){
					e.printStackTrace();
				}
			}
	
		
	};
	
	
	
	this.stop= false;
	this.serverThread.start();
	
	}//run
	
	
	public void stop(){
		
		this.stop = true;		
				try (Socket socket = new Socket("localhost", server_port)) {
					// Esta conexión se hace, simplemente, para "despertar" el hilo servidor
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				try {
					this.serverThread.join();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}		
	}
	
	
	
//Metodo para la generacion de enlaces con el puerto del servidor desde router.	
	public int getPort(){
		return server_port;
	}

}

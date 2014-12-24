package es.uvigo.esei.dai.hybridserver;



////// 	AUTORES	/////

/************************************/
/*	JOSE JUAN DIOS OTERO			*/	
/*			Y						*/
/*	MAIECO RODRIGUES DIEZ			*/
/*									*/
/************************************/


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class Launcher {
	
	public static void main(String[] args) {
		System.out.println("empiezo");
		File configurationFile=null;
		File validation = new File("src/es/uvigo/esei/dai/hybridserver/http/configuracion.xsd");
		
		//Si no hay argumentos se cargar el por defecto
		if(args.length==0){
			//para pruebas comento esto y cargo directamente el configuracion.xml
			//configurationFile=new File(args[1]);
			configurationFile=new File("src/es/uvigo/esei/dai/hybridserver/http/configuracion.xml");
			System.out.println(validation.getAbsolutePath());
			System.out.println(validation.getName());
			if(validateXMLSchema(validation, configurationFile)){
				XMLConfigurationLoader loader = new XMLConfigurationLoader();
				try {
					Configuration configuration = loader.load(configurationFile);
					HybridServer server = new HybridServer(configuration);
					System.out.println("arranco hybridserver");
					server.start();
				} catch (Exception e) {
					e.printStackTrace();
					System.err.println("Archivo de configuración erroneo, imposible cargar");
				}
				
			}else{
				
				//Cargamos las propiedades del archivo config.conf
				
				Properties properties= loadPropertiesFromFile(configurationFile);
					
					//Si no hay problemas en la lectura del archivo, en caso contrario se cargaran la por defecto del HybridServer
				    if(properties!=null){
				    	HybridServer server = new HybridServer(properties);
				    	server.start();
				    }else{	
				    	
					//Instanciamos con las propiedades por defecto
					HybridServer server = new HybridServer();
					//El servidor tiene que ser un hilo para luego poder pararlo.
					server.start();
				}
			}
		}
		
		

	}

	private static Properties loadPropertiesFromFile(File f) {
		
		try{
			
			Properties properts = new Properties();
			properts.load(new FileInputStream(f));
			return properts;
			
		} catch (IOException e) {
			return null;
		}
		
	}
	
	
	public static boolean validateXMLSchema(File xsd, File xml){
        
        try {
            SchemaFactory factory = 
                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsd);
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
        } catch (IOException | SAXException e) {
            System.out.println("Exception: "+e.getMessage());
            return false;
        }
        return true;
    }
	
	

}

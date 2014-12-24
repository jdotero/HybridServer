package es.uvigo.esei.dai.hybridserver.http;

public class XML implements Resource{
	public XML(){
		this.uuid=null;
		this.content=null;
		this.length=0;
	}
	
	public XML(String id, String cont){
		this.uuid=id;
		this.content=cont;
		if(this.uuid!=null){
			this.length=cont.length();
			}else{
			this.length=0;
			}
	}
	
	@Override
	public String getResourceName() {
		return this.name;
	}

	
	public void setUUID(String id){
		this.uuid=id;
	}
	
	
	public void setContent(String content){
		this.content=content;
		this.length=content.length();
	}
	
		
	public String getUUID(){
		return this.uuid;
	}
	
	
	public String getContent(){
		return this.content;
	}
	
	public String getType(){
		return this.type;
	}
	
	public int getLength(){
		return this.length;
	}
	
	public String getAuxResource(){
		return this.aux;
	}

	
	private  String uuid;
	private  String content;
	private  String aux=null;
	private  String type="application/xml";
	private  int length;
	private  String name="xml";

}

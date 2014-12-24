package es.uvigo.esei.dai.hybridserver.http;

public class HTML implements Resource {
	public HTML(){
		this.uuid=null;
		this.content=null;
		this.length=0;
	}
	public HTML(String id, String cont){
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
	
	@Override
	public String getAuxResource() {
		return this.aux;
	}

	
	private  String uuid;
	private  String content;
	private  String aux=null;
	private  String type="text/html";
	private  int length;
	private  String name="html";
	
	
	
	
}
	
	
	

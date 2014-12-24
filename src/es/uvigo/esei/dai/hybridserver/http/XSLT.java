package es.uvigo.esei.dai.hybridserver.http;

public class XSLT implements Resource{
	public XSLT(){
		this.uuid=null;
		this.content=null;
		this.uuidxsd=null;
		this.length=0;
	}
	public XSLT(String id, String cont, String aux){
		this.uuid=id;
		this.content=cont;
		this.uuidxsd=aux;
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
	
	public void setAuxResource(String uuid){
		this.uuidxsd=uuid;
	}
	
		
	public String getUUID(){
		return this.uuid;
	}
	
	
	public String getContent(){
		return this.content;
	}
	
	public String getXSD(){
		return this.uuidxsd;
	}
	
	public String getType(){
		return this.type;
	}
	
	public int getLength(){
		return this.length;
	}
	@Override
	public String getAuxResource(){
		return this.uuidxsd;
	}

	
	private  String uuid;
	private  String content;
	private  String uuidxsd;
	private  String type="application/xml";
	private  int length;
	private  String name="xslt";

}

package es.uvigo.esei.dai.hybridserver.http;

public interface Resource {

	public void setUUID(String id);
	public void setContent(String content);
	public String getUUID();
	public String getResourceName();
	public String getAuxResource();
	public String getContent();
	public String getType();
	public int getLength();
	
	
}

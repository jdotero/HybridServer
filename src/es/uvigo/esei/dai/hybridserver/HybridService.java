package es.uvigo.esei.dai.hybridserver;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import es.uvigo.esei.dai.hybridserver.http.HTML;
import es.uvigo.esei.dai.hybridserver.http.XML;
import es.uvigo.esei.dai.hybridserver.http.XSD;
import es.uvigo.esei.dai.hybridserver.http.XSLT;


@WebService
@SOAPBinding(style=Style.RPC)
public interface HybridService {
	@WebMethod
	public String getLinksAllResources(String type);
	@WebMethod
	public HTML getHTMLResource(String uuid );
	@WebMethod
	public XML getXMLResource(String uuid );
	@WebMethod
	public XSD getXSDResource(String uuid );
	@WebMethod
	public XSLT getXSLTResource(String uuid );
	@WebMethod
	public boolean removeRestourceOfType(String type, String uuid);
}

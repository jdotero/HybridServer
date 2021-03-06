package es.uvigo.esei.dai.hybridserver.week1;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import es.uvigo.esei.dai.hybridserver.http.HTTPHeaders;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequest;
import es.uvigo.esei.dai.hybridserver.http.HTTPRequestMethod;

public class HTTPRequestGETResourcesTest {
	private String requestText;
	private HTTPRequest request;
	
	@Before
	public void setUp() throws Exception {
		this.requestText = "GET /hello/world.html HTTP/1.1\n" +
			"Host: localhost\n" +
			"Accept: text/html\n" +
			"Accept-Encoding: gzip,deflate\n";
		
		this.request = new HTTPRequest(new StringReader(
			this.requestText + "\n"
		));
	}

	@Test
	public final void testGetMethod() {
		assertEquals(HTTPRequestMethod.GET, this.request.getMethod());
	}

	@Test
	public final void testGetResourceChain() {
		assertEquals("/hello/world.html", this.request.getResourceChain());
	}

	@Test
	public final void testGetResourceName() {
		assertEquals("hello/world.html", this.request.getResourceName());
	}

	@Test
	public final void testGetResourceParameters() {
		assertEquals(Collections.emptyMap(), this.request.getResourceParameters());
	}

	@Test
	public final void testGetHttpVersion() {
		assertEquals(HTTPHeaders.HTTP_1_1.getHeader(), this.request.getHttpVersion());
	}

	@Test
	public final void testGetHeaderParameters() {
		final Map<String, String> parameters = new HashMap<>();
		parameters.put("Host", "localhost");
		parameters.put("Accept", "text/html");
		parameters.put("Accept-Encoding", "gzip,deflate");
		
		assertEquals(parameters, this.request.getHeaderParameters());
	}

	@Test
	public final void testGetContent() {
		assertEquals(null, this.request.getContent());
	}

	@Test
	public final void testGetContentLength() {
		assertEquals(0, this.request.getContentLength());
	}

	@Test
	public final void testToString() {
		assertEquals(this.requestText, this.request.toString());
	}

}

package sneer.pulp.dyndns.checkip.tests;

import static sneer.commons.environments.Environments.my;

import java.io.IOException;

import org.jmock.Expectations;
import org.junit.Test;

import sneer.brickness.testsupport.BrickTest;
import sneer.brickness.testsupport.Contribute;
import sneer.pulp.dyndns.checkip.CheckIp;
import sneer.pulp.httpclient.HttpClient;

public class CheckIpTest extends BrickTest {
	
	@Contribute final HttpClient _client = mock(HttpClient.class);
	
	@Test
	public void test() throws IOException {
		
		final String ip = "123.456.78.90";
		final String responseBody = 
			"<html><head><title>Current IP Check</title></head><body>Current IP Address: "
			+ ip
			+ "</body></html>";
		
		checking(new Expectations() {{
			one(_client).get("http://checkip.dyndns.org/"); will(returnValue(responseBody));
		}});
		
		final CheckIp checkIp = my(CheckIp.class);
		assertEquals(ip, checkIp.check());
		
	}
}
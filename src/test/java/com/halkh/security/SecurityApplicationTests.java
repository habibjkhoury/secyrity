package com.halkh.security;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class SecurityApplicationTests {

	URL base;
	@LocalServerPort
	int port;

	JSONObject userJsonObject;
	HttpHeaders headers;

	RestTemplate restTemplate;

	@Before
	public void setUp() throws MalformedURLException, JSONException {

		restTemplate = new RestTemplate();
		headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		userJsonObject = new JSONObject();
		userJsonObject.put("username", "best@best.com");
		userJsonObject.put("password", "test");

		base = new URL("http://localhost:" + port + "/login");
	}

	@Test
	public void whenLoggedUserRequestsHomePage_ThenSuccess()
			throws IllegalStateException, MalformedURLException {

		HttpEntity<String> loginRequest =
				new HttpEntity<>(userJsonObject.toString(), headers);

		String token =
				restTemplate.postForObject(base.toString(), loginRequest, String.class); //login returns token

		headers.set("Authorization", token);

		assertNotNull(token); //login successful

		URL securedUrl = new URL("http://localhost:" + port + "/secured");

		HttpEntity<String> entity = new HttpEntity<>("body", headers);
		ResponseEntity<String> securedResponse = restTemplate.exchange(securedUrl.toString(), HttpMethod.GET, entity, String.class);

		assertEquals(securedResponse.getStatusCode(), HttpStatus.OK);
		assertEquals("Welcome to Secured App", securedResponse.getBody());

	}


	@Test(expected = HttpClientErrorException.Unauthorized.class)
	public void whenNotLoggedUserRequestsHomePage_ThenFailure()
			throws IllegalStateException, MalformedURLException {


		headers.set("Authorization", null);

		URL securedUrl = new URL("http://localhost:" + port + "/secured");

		HttpEntity<String> entity = new HttpEntity<>("body", headers);


		restTemplate.exchange(securedUrl.toString(), HttpMethod.GET, entity, String.class);

	}

}

package com.wiremock;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static org.junit.Assert.assertEquals;

public class WireMockTest {

    @Rule
    public WireMockRule rule = new WireMockRule();

    private static String GET_USERS = "http://localhost:8080/users/12345";
    private static String POST_USER = "http://localhost:8080/users";
    private static String USER_CONTENT = "<users><user><id>12345</id><name>bala</name></user></users>";

    JerseyClient client;

    @Before
    public void setUp() {
        client = JerseyClientBuilder.createClient();
    }

    @After
    public void tearDowm() {
        rule.shutdown();
    }

    private void stubFor(){
        rule.stubFor(get(urlEqualTo("/users/12345")).willReturn(
                aResponse().withHeader("Content-Type", "application/xml")
                        .withBody(USER_CONTENT)));
        rule.stubFor(post(urlEqualTo("/users")).willReturn(aResponse().withStatus(200)));

    }

    @Test
    public void testAddUser() {
        stubFor();
        client.target(POST_USER).request().post(entity(USER_CONTENT, APPLICATION_XML_TYPE));
        rule.verify(postRequestedFor(urlMatching("/users"))
                            .withRequestBody(matching(USER_CONTENT))
                            .withHeader("Content-Type", matching("application/xml")));

    }

    @Test
    public void testGetUser() {
        stubFor();
        Response response = client.target(GET_USERS).request().get();
        String output = response.readEntity(String.class);
        System.out.println("output: "+output);
        assertEquals(200, response.getStatus());
        assertEquals(USER_CONTENT, output);
    }

}

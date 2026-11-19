package com.qa.framework.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.qa.framework.specs.RequestSpecs;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Service virtualization: a local stand-in service replaces the real one.
 * This lets us test error codes, slow responses and unavailable services that are
 * hard or impossible to trigger on a real environment. Runs offline.
 * (Parasoft Virtualize plays the same role in a Parasoft setup.)
 */
public class VirtualizedRestTest extends BaseApiTest {

    private WireMockServer server;

    @BeforeClass(alwaysRun = true)
    public void startVirtualService() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();

        server.stubFor(get(urlEqualTo("/users/1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":1,\"name\":\"Test User\",\"active\":true}")));

        server.stubFor(get(urlEqualTo("/users/401"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Unauthorized\"}")));

        server.stubFor(get(urlEqualTo("/users/500"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"Internal Server Error\"}")));

        server.stubFor(get(urlEqualTo("/users/slow"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(1500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":99,\"name\":\"Slow User\"}")));
    }

    @AfterClass(alwaysRun = true)
    public void stopVirtualService() {
        if (server != null) {
            server.stop();
        }
    }

    @Test(groups = {"virtual", "smoke"}, description = "TC-110 happy path from the virtual service")
    public void getUser_returnsStubbedData() {
        given().spec(RequestSpecs.json(server.baseUrl()))
                .when().get("/users/1")
                .then()
                .statusCode(200)
                .body("name", equalTo("Test User"))
                .body("active", equalTo(true));
    }

    @Test(groups = "virtual", description = "TC-111 unauthorized is handled and returns a clear error")
    public void getUser_unauthorized() {
        given().spec(RequestSpecs.json(server.baseUrl()))
                .when().get("/users/401")
                .then()
                .statusCode(401)
                .body("error", equalTo("Unauthorized"));
    }

    @Test(groups = "virtual", description = "TC-112 server error scenario that is hard to trigger on a real system")
    public void getUser_serverError() {
        given().spec(RequestSpecs.json(server.baseUrl()))
                .when().get("/users/500")
                .then()
                .statusCode(500)
                .body("error", equalTo("Internal Server Error"));
    }

    @Test(groups = "virtual", description = "TC-113 slow dependency: the call takes at least the simulated delay")
    public void getUser_slowService() {
        given().spec(RequestSpecs.json(server.baseUrl()))
                .when().get("/users/slow")
                .then()
                .statusCode(200)
                .time(greaterThan(1400L));
    }
}

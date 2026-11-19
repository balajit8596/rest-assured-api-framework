package com.qa.framework.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * SOAP testing with REST Assured: a SOAP call is just an HTTP POST with an XML envelope.
 * The service is virtualized, so the test runs offline. The matching WSDL is in
 * parasoft/soap/calculator.wsdl so the same service can be imported into Parasoft SOAtest.
 */
public class SoapServiceTest extends BaseApiTest {

    private static final String ENDPOINT = "/soap/calculator";

    private static final String ADD_REQUEST = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <Add xmlns="http://tempuri.org/">
                  <a>2</a>
                  <b>3</b>
                </Add>
              </soap:Body>
            </soap:Envelope>
            """;

    private static final String DIVIDE_BY_ZERO_REQUEST = """
            <?xml version="1.0" encoding="utf-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <Divide xmlns="http://tempuri.org/">
                  <a>10</a>
                  <b>0</b>
                </Divide>
              </soap:Body>
            </soap:Envelope>
            """;

    private WireMockServer server;

    @BeforeClass(alwaysRun = true)
    public void startVirtualSoapService() {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();

        server.stubFor(post(urlEqualTo(ENDPOINT))
                .withRequestBody(containing("<Add "))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/xml; charset=utf-8")
                        .withBody("""
                                <Envelope xmlns="http://schemas.xmlsoap.org/soap/envelope/">
                                  <Body>
                                    <AddResponse xmlns="http://tempuri.org/">
                                      <AddResult>5</AddResult>
                                    </AddResponse>
                                  </Body>
                                </Envelope>
                                """)));

        server.stubFor(post(urlEqualTo(ENDPOINT))
                .withRequestBody(containing("<Divide "))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "text/xml; charset=utf-8")
                        .withBody("""
                                <Envelope xmlns="http://schemas.xmlsoap.org/soap/envelope/">
                                  <Body>
                                    <Fault>
                                      <faultcode>Server</faultcode>
                                      <faultstring>Division by zero</faultstring>
                                    </Fault>
                                  </Body>
                                </Envelope>
                                """)));
    }

    @AfterClass(alwaysRun = true)
    public void stopVirtualSoapService() {
        if (server != null) {
            server.stop();
        }
    }

    @Test(groups = {"virtual", "smoke"}, description = "TC-120 SOAP Add returns the correct result")
    public void soapAdd_returnsSum() {
        given().baseUri(server.baseUrl())
                .contentType("text/xml; charset=utf-8")
                .header("SOAPAction", "http://tempuri.org/Add")
                .body(ADD_REQUEST)
                .when().post(ENDPOINT)
                .then()
                .statusCode(200)
                .body("Envelope.Body.AddResponse.AddResult", equalTo("5"));
    }

    @Test(groups = "virtual", description = "TC-121 SOAP fault is returned for invalid input")
    public void soapDivideByZero_returnsFault() {
        given().baseUri(server.baseUrl())
                .contentType("text/xml; charset=utf-8")
                .header("SOAPAction", "http://tempuri.org/Divide")
                .body(DIVIDE_BY_ZERO_REQUEST)
                .when().post(ENDPOINT)
                .then()
                .statusCode(500)
                .body("Envelope.Body.Fault.faultstring", equalTo("Division by zero"));
    }
}

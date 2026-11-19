package com.qa.framework.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.qa.framework.db.DbValidator;
import com.qa.framework.specs.RequestSpecs;
import io.restassured.path.json.JsonPath;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Backend validation pattern: call the API, then use SQL to confirm the stored data matches.
 * An in-memory H2 database stands in for MySQL so the test runs anywhere.
 * With a real project, only the JDBC URL and credentials change.
 */
public class ApiVsDatabaseTest extends BaseApiTest {

    private WireMockServer server;
    private Connection connection;
    private DbValidator db;

    @BeforeClass(alwaysRun = true)
    public void setUp() throws SQLException {
        server = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        server.start();
        server.stubFor(get(urlEqualTo("/payments/TXN1001"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"transactionId\":\"TXN1001\",\"status\":\"COMPLETED\","
                                + "\"amount\":250.50,\"currency\":\"EUR\"}")));

        connection = DriverManager.getConnection("jdbc:h2:mem:paymentsdb;DB_CLOSE_DELAY=-1");
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE payments ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "transaction_id VARCHAR(20), status VARCHAR(20), "
                    + "amount DECIMAL(10,2), currency VARCHAR(3))");
        }
        db = new DbValidator(connection);
        db.execute("INSERT INTO payments (transaction_id, status, amount, currency) VALUES (?,?,?,?)",
                "TXN1001", "COMPLETED", new BigDecimal("250.50"), "EUR");
        db.execute("INSERT INTO payments (transaction_id, status, amount, currency) VALUES (?,?,?,?)",
                "TXN2002", "PENDING", new BigDecimal("40.00"), "GBP");
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
        if (server != null) {
            server.stop();
        }
    }

    @Test(groups = {"virtual", "db"}, description = "TC-130 API response matches the database record")
    public void apiResponse_matchesDatabaseRecord() throws SQLException {
        JsonPath api = given().spec(RequestSpecs.json(server.baseUrl()))
                .when().get("/payments/TXN1001")
                .then().statusCode(200)
                .extract().jsonPath();

        Map<String, Object> row = db.findOne(
                "SELECT status, amount, currency FROM payments WHERE transaction_id = ?",
                api.getString("transactionId"));

        assertEquals(row.get("status"), api.getString("status"), "status differs between API and DB");
        assertEquals(row.get("currency"), api.getString("currency"), "currency differs between API and DB");

        BigDecimal dbAmount = (BigDecimal) row.get("amount");
        BigDecimal apiAmount = new BigDecimal(api.getString("amount"));
        assertEquals(dbAmount.compareTo(apiAmount), 0, "amount differs between API and DB");
    }

    @Test(groups = {"virtual", "db"}, description = "TC-131 a duplicate payment is detected with SQL")
    public void duplicatePayments_areDetected() throws SQLException {
        String duplicateSql = "SELECT transaction_id, COUNT(*) AS cnt FROM payments "
                + "GROUP BY transaction_id HAVING COUNT(*) > 1";

        assertTrue(db.query(duplicateSql).isEmpty(), "no duplicates expected at the start");

        db.execute("INSERT INTO payments (transaction_id, status, amount, currency) VALUES (?,?,?,?)",
                "TXN2002", "PENDING", new BigDecimal("40.00"), "GBP");

        List<Map<String, Object>> duplicates = db.query(duplicateSql);
        assertEquals(duplicates.size(), 1);
        assertEquals(duplicates.get(0).get("transaction_id"), "TXN2002");

        // Clean up so other tests see the original data.
        db.execute("DELETE FROM payments WHERE transaction_id = ? AND id = "
                + "(SELECT MAX(id) FROM payments WHERE transaction_id = ?)", "TXN2002", "TXN2002");
    }
}

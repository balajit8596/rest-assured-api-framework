package com.qa.framework.tests;

import io.restassured.RestAssured;
import org.testng.annotations.BeforeClass;

/** Common setup. When an assertion fails, the full request and response are printed. */
public abstract class BaseApiTest {

    @BeforeClass(alwaysRun = true)
    public void configureRestAssured() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}

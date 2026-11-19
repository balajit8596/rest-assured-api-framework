package com.qa.framework.specs;

import com.qa.framework.config.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.lessThan;

/** Reusable request and response specifications, so tests never repeat headers or base URLs. */
public final class RequestSpecs {

    private RequestSpecs() {
    }

    /** JSON request spec for the given base URI. */
    public static RequestSpecification json(String baseUri) {
        return new RequestSpecBuilder()
                .setBaseUri(baseUri)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    /** Response must come back within the configured time limit. */
    public static ResponseSpecification fastResponse() {
        return new ResponseSpecBuilder()
                .expectResponseTime(lessThan(Config.maxResponseMs()), TimeUnit.MILLISECONDS)
                .build();
    }
}

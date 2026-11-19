package com.qa.framework.api;

import com.qa.framework.models.Post;
import com.qa.framework.specs.RequestSpecs;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Service object for the posts endpoints. It only sends requests and returns the Response.
 * Assertions stay in the tests, so one request method serves positive and negative cases.
 */
public class PostsApi {

    private final RequestSpecification spec;

    public PostsApi(String baseUri) {
        this.spec = RequestSpecs.json(baseUri);
    }

    public Response getAll() {
        return given().spec(spec).when().get("/posts");
    }

    public Response getById(int id) {
        return given().spec(spec).when().get("/posts/{id}", id);
    }

    public Response getByUser(int userId) {
        return given().spec(spec).queryParam("userId", userId).when().get("/posts");
    }

    public Response create(Post post) {
        return given().spec(spec).body(post).when().post("/posts");
    }

    public Response update(int id, Post post) {
        return given().spec(spec).body(post).when().put("/posts/{id}", id);
    }

    public Response delete(int id) {
        return given().spec(spec).when().delete("/posts/{id}", id);
    }
}

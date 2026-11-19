package com.qa.framework.tests;

import com.qa.framework.api.PostsApi;
import com.qa.framework.config.Config;
import com.qa.framework.data.TestDataFactory;
import com.qa.framework.models.Post;
import com.qa.framework.specs.RequestSpecs;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * REST tests against a live public API (JSONPlaceholder).
 * Needs internet access. Run only these with: mvn test -Dgroups=live
 */
public class PostsApiTest extends BaseApiTest {

    private final PostsApi api = new PostsApi(Config.baseUrl());

    @Test(groups = {"live", "smoke"}, description = "TC-100 list returns 200 and data")
    public void getAllPosts_returnsList() {
        api.getAll()
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }

    @Test(groups = {"live", "smoke"}, description = "TC-101 single post matches the JSON schema")
    public void getPost_matchesSchema() {
        api.getById(1)
                .then()
                .statusCode(200)
                .body(matchesJsonSchemaInClasspath("schemas/post-schema.json"))
                .body("id", equalTo(1));
    }

    @Test(groups = "live", description = "TC-102 create returns 201 and echoes the data")
    public void createPost_returns201() {
        Post request = TestDataFactory.newPost();

        Post created = api.create(request)
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .as(Post.class);

        assertEquals(created.getTitle(), request.getTitle());
        assertEquals(created.getUserId(), request.getUserId());
        assertNotNull(created.getId());
    }

    @Test(groups = "live", description = "TC-103 update returns 200 with the new data")
    public void updatePost_returns200() {
        Post update = TestDataFactory.newPost();
        update.setId(1);
        update.setTitle("updated title");

        api.update(1, update)
                .then()
                .statusCode(200)
                .body("title", equalTo("updated title"));
    }

    @Test(groups = "live", description = "TC-104 delete returns 200")
    public void deletePost_returns200() {
        api.delete(1).then().statusCode(200);
    }

    @Test(groups = "live", description = "TC-105 unknown resource returns 404")
    public void getUnknownPost_returns404() {
        api.getById(999999).then().statusCode(404);
    }

    @Test(groups = "live", description = "TC-106 filter by query parameter")
    public void getPostsByUser_onlyReturnsThatUser() {
        api.getByUser(1)
                .then()
                .statusCode(200)
                .body("userId", everyItem(equalTo(1)));
    }

    @Test(groups = "live", description = "TC-107 response time is within the limit")
    public void getPost_respondsQuickly() {
        api.getById(1).then().spec(RequestSpecs.fastResponse());
    }

    @Test(groups = "live", description = "TC-108 chain: use data from one call in the next")
    public void chainedCalls_secondCallUsesFirstResponse() {
        int firstId = api.getByUser(1)
                .then()
                .statusCode(200)
                .extract()
                .path("[0].id");

        Response second = api.getById(firstId);
        assertEquals(second.statusCode(), 200);
        assertEquals(second.jsonPath().getInt("id"), firstId);
    }

    @DataProvider(name = "postIds")
    public Object[][] postIds() {
        return new Object[][]{{1}, {2}, {3}, {4}, {5}};
    }

    @Test(groups = "live", dataProvider = "postIds", description = "TC-109 data driven: several ids")
    public void getPost_dataDriven(int id) {
        api.getById(id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id));
    }
}

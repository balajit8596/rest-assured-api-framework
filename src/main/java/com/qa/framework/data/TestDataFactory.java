package com.qa.framework.data;

import com.qa.framework.models.Post;

/** Builds unique test data so runs never collide with each other. */
public final class TestDataFactory {

    private TestDataFactory() {
    }

    public static Post newPost() {
        return new Post(1, "qa-post-" + System.currentTimeMillis(), "Created by the REST Assured framework");
    }
}

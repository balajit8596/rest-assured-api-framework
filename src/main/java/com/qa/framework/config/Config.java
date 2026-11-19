package com.qa.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central configuration.
 * Lookup order: JVM system property, then environment variable, then config.properties.
 * Example: -Dapi.base.url=https://staging.example.com  or  API_BASE_URL=https://staging.example.com
 */
public final class Config {

    private static final Properties PROPS = load();

    private Config() {
    }

    private static Properties load() {
        Properties props = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read config.properties", e);
        }
        return props;
    }

    public static String get(String key, String fallback) {
        String system = System.getProperty(key);
        if (system != null && !system.isBlank()) {
            return system;
        }
        String env = System.getenv(key.replace('.', '_').toUpperCase());
        if (env != null && !env.isBlank()) {
            return env;
        }
        return PROPS.getProperty(key, fallback);
    }

    public static String baseUrl() {
        return get("api.base.url", "https://jsonplaceholder.typicode.com");
    }

    public static long maxResponseMs() {
        return Long.parseLong(get("api.max.response.ms", "3000"));
    }
}

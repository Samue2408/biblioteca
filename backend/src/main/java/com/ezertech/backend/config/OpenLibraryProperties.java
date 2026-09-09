package com.ezertech.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.openlibrary")
public record OpenLibraryProperties(String baseUrl, String coversBaseUrl, int timeoutSeconds) {

}
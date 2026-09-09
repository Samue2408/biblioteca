package com.ezertech.backend.client.openlibrary;

import com.ezertech.backend.exception.ExternalBookLookupException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;

@Component
public class OpenLibraryClient {

    private final RestClient restClient;

    public OpenLibraryClient(RestClient openLibraryRestClient) {
        this.restClient = openLibraryRestClient;
    }

    @Cacheable(value = "openLibraryLookup", key = "#isbn")
    public Optional<OpenLibraryBookData> lookupByIsbn(String isbn) {
        try {
            Map<String, OpenLibraryBookData> response = restClient.get()
                    .uri("/api/books?bibkeys=ISBN:{isbn}&format=json&jscmd=data", isbn)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, OpenLibraryBookData>>() {});

            if (response == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(response.get("ISBN:" + isbn));

        } catch (RestClientException e) {
            throw new ExternalBookLookupException(
                    "No se pudo consultar Open Library para el ISBN " + isbn, e);
        }
    }
}

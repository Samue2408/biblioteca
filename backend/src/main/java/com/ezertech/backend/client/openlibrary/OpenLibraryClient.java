package com.ezertech.backend.client.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ezertech.backend.exception.ExternalBookLookupException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
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
            OpenLibraryEditionResponse response = restClient.get()
                .uri("/isbn/{isbn}.json", isbn)
                    .retrieve()
                .body(OpenLibraryEditionResponse.class);

            if (response == null) {
                return Optional.empty();
            }

            List<OpenLibraryAuthor> authors = response.authors() == null
                ? List.of()
                : response.authors().stream()
                .map(OpenLibraryAuthorReference::key)
                .map(this::lookupAuthorName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(OpenLibraryAuthor::new)
                .toList();

            OpenLibraryCover cover = response.covers() == null || response.covers().isEmpty()
                ? null
                : new OpenLibraryCover(
                "https://covers.openlibrary.org/b/id/" + response.covers().get(0) + "-S.jpg",
                "https://covers.openlibrary.org/b/id/" + response.covers().get(0) + "-M.jpg",
                "https://covers.openlibrary.org/b/id/" + response.covers().get(0) + "-L.jpg");

            return Optional.of(new OpenLibraryBookData(
                response.title(), authors, response.publishDate(), cover));

        } catch (RestClientException e) {
            throw new ExternalBookLookupException(
                    "No se pudo consultar Open Library para el ISBN " + isbn, e);
        }
    }

        private Optional<String> lookupAuthorName(String authorKey) {
        if (authorKey == null || authorKey.isBlank()) {
            return Optional.empty();
        }

        String authorId = authorKey.substring(authorKey.lastIndexOf('/') + 1);
        OpenLibraryAuthorResponse response = restClient.get()
            .uri("/authors/{authorId}.json", authorId)
            .retrieve()
            .body(OpenLibraryAuthorResponse.class);

        return response == null ? Optional.empty() : Optional.ofNullable(response.name());
        }

        private record OpenLibraryEditionResponse(
            String title,
            List<OpenLibraryAuthorReference> authors,
            @JsonProperty("publish_date") String publishDate,
            List<Integer> covers
        ) {
        }

        private record OpenLibraryAuthorReference(String key) {
        }

        private record OpenLibraryAuthorResponse(String name) {
        }
}

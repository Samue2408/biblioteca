package com.ezertech.backend.client.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenLibraryBookData(
        String title,
        List<OpenLibraryAuthor> authors,
        @JsonProperty("publish_date") String publishDate,
        OpenLibraryCover cover
) {
}

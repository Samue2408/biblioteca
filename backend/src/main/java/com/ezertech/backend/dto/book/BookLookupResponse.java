package com.ezertech.backend.dto.book;

public record BookLookupResponse (
        String title, String author, Integer publicationYear, String coverUrl
){
}

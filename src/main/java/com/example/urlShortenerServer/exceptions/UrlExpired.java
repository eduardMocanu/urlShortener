package com.example.urlShortenerServer.exceptions;

public class UrlExpired extends RuntimeException {
    public UrlExpired(String message) {
        super(message);
    }
}

package com.example.urlShortenerServer.exceptions;

public class UrlNotFound extends RuntimeException {
    public UrlNotFound(String message) {
        super(message);
    }
}

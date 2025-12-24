package com.example.urlShortenerServer.exceptions;

public class InvalidUrl extends RuntimeException {
    public InvalidUrl(String message) {
        super(message);
    }
}

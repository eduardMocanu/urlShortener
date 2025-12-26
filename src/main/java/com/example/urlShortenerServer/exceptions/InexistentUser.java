package com.example.urlShortenerServer.exceptions;

public class InexistentUser extends RuntimeException {
    public InexistentUser(String message) {
        super(message);
    }
}

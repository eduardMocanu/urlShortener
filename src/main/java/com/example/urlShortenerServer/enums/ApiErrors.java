package com.example.urlShortenerServer.enums;

public enum ApiErrors {
    URL_NOT_FOUND,
    URL_EXPIRED,
    INVALID_URL,
    SHORT_URL_COLLISION,
    INTERNAL_ERROR,
    USERNAME_EXISTS_ALREADY,
    INEXISTENT_USER,
    UNAUTHORIZED
}

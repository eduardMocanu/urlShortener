package com.example.urlShortenerServer.exceptions;

public class UsernameExistsAlready extends RuntimeException{
    public UsernameExistsAlready(String message){super(message);}
}

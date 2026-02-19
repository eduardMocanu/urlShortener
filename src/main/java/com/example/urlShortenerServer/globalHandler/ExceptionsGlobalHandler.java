package com.example.urlShortenerServer.globalHandler;

import com.example.urlShortenerServer.enums.ApiErrors;
import com.example.urlShortenerServer.error.ApiError;
import com.example.urlShortenerServer.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionsGlobalHandler {
    private static final Logger log =
            LoggerFactory.getLogger(ExceptionsGlobalHandler.class);

    @Value("${frontend.url}")
    private String frontend_url;


    @ExceptionHandler(UrlExpired.class)
    public ResponseEntity<Void> handleUrlExpiredException() {
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(frontend_url + "/expired"))
                .build();
    }

    @ExceptionHandler(UrlNotFound.class)
    public ResponseEntity<Void> handleUrlNotFoundException() {
        return ResponseEntity
                .status(HttpStatus.FOUND)
                .location(URI.create(frontend_url + "/not-found"))
                .build();
    }

    @ExceptionHandler(UsernameExistsAlready.class)
    public ResponseEntity<?> handleUsernameExistsAlready(UsernameExistsAlready usernameExistsAlready, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiError.builder()
                        .timeStamp(LocalDateTime.now())
                        .status(HttpStatus.CONFLICT)
                        .error(ApiErrors.USERNAME_EXISTS_ALREADY)
                        .message("The given username already exists in the db")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(Unauthorized.class)
    public ResponseEntity<?> handleUnauthorized(Unauthorized unauthorized, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiError.builder()
                        .timeStamp(LocalDateTime.now())
                        .status(HttpStatus.UNAUTHORIZED)
                        .error(ApiErrors.UNAUTHORIZED)
                        .message("You are not authorized to do this")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(DataIntegrityViolationException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Tried to input duplicate values on an unique column");
    }

    @ExceptionHandler(exception = UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFund(InexistentUser ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiError.builder()
                        .timeStamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND)
                        .error(ApiErrors.INEXISTENT_USER)
                        .message("The credentials for the wanted user are not in the db")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(exception = InexistentUser.class)
    public ResponseEntity<?> handleInexistentUser(InexistentUser ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiError.builder()
                        .timeStamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_FOUND)
                        .error(ApiErrors.INEXISTENT_USER)
                        .message("The credentials for the wanted user are not in the db")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(InvalidUrl.class)
    public ResponseEntity<?> handleInvalidUrl(InvalidUrl ex, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ApiError.builder()
                        .timeStamp(LocalDateTime.now())
                        .status(HttpStatus.BAD_REQUEST)
                        .error(ApiErrors.INVALID_URL)
                        .message("The given url is invalid")
                        .path(request.getRequestURI())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception, HttpServletRequest request){
        log.error("Unexpected exception message = {}, localized = {}, on path = {}", exception.getMessage(), exception.getLocalizedMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiError.builder()
                                .timeStamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .error(ApiErrors.INTERNAL_ERROR)
                                .message("Internal server error")
                                .path(request.getRequestURI())
                                .build()
                );
    }
}

package com.example.urlShortenerServer.globalHandler;

import com.example.urlShortenerServer.controller.UrlController;
import com.example.urlShortenerServer.enums.ApiErrors;
import com.example.urlShortenerServer.error.ApiError;
import com.example.urlShortenerServer.exceptions.UrlExpired;
import com.example.urlShortenerServer.exceptions.UrlNotFound;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionsGlobalHandler {
    private static final Logger log =
            LoggerFactory.getLogger(ExceptionsGlobalHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception exception, HttpServletRequest request){
        log.error("Unexpected exception message = {}, localized = {}, stack trace = {}, on path = {}", exception.getMessage(), exception.getLocalizedMessage(), exception.getStackTrace(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        ApiError.builder()
                                .timeStamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .error(ApiErrors.INTERNAL_ERROR)
                                .message("Internal server error")
                                .path(request.getRequestURI())
                );
    }

    @ExceptionHandler(UrlExpired.class)
    public ResponseEntity<?> handleUrlExpiredException(UrlExpired expired, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.builder().timeStamp(LocalDateTime.now()).status(HttpStatus.NOT_FOUND).error(ApiErrors.URL_EXPIRED).message("The wanted url is expired").path(request.getRequestURI()));
    }

    @ExceptionHandler(UrlNotFound.class)
    public ResponseEntity<?> handleUrlNotFoundException(UrlNotFound notFound, HttpServletRequest request){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.builder().timeStamp(LocalDateTime.now()).status(HttpStatus.NOT_FOUND).error(ApiErrors.URL_NOT_FOUND).message("The wanted url is not found").path(request.getRequestURI()));
    }
}

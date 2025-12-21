package com.example.urlShortenerServer.error;

import com.example.urlShortenerServer.enums.ApiErrors;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ApiError {
    private LocalDateTime timeStamp;
    private HttpStatus status;
    private ApiErrors error;
    private String message;
    private String path;
}

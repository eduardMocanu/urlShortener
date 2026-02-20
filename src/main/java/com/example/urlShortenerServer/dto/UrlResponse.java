package com.example.urlShortenerServer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
public class UrlResponse {
    private Long id;
    private String url;
    private String shortUrl;
    private LocalDateTime createdAt;
    private LocalDateTime expiration;
    private LocalDateTime lastAccessed;
    private Long clicksCount;
    private Boolean active;
}

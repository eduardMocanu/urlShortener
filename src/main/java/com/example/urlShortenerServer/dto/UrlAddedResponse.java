package com.example.urlShortenerServer.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UrlAddedResponse {
    private String code;
    private String fullShortUrl;
}

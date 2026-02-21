package com.example.urlShortenerServer.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class UrlExtensionDto {

    private Long id;
    private Integer extensions;
    private LocalDateTime expiration;
    private Integer maximumExtensions = 5;

}

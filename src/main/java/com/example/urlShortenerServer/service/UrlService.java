package com.example.urlShortenerServer.service;

import com.example.urlShortenerServer.controller.UrlController;
import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.exceptions.UrlExpired;
import com.example.urlShortenerServer.exceptions.UrlNotFound;
import com.example.urlShortenerServer.repository.UrlRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UrlService {
    private static final Logger log =
            LoggerFactory.getLogger(UrlService.class);

    private UrlRepository urlRepository;
    @Autowired
    public void setRepository(UrlRepository repository) {
        this.urlRepository = repository;
    }

    @Transactional//allows dirty checking
    public String findUrlByCode(String code) throws UrlExpired, UrlNotFound{
        Url url = urlRepository.findByShortUrl(code).orElseThrow(() -> {
            log.warn("The wanted url for the code = {} is not found", code);
            return new UrlNotFound("There is no url found in the database by that code");
        });

        if (!url.getActive() || LocalDateTime.now().isAfter(url.getExpiration())){
            url.setActive(false);
            log.warn("The wanted url = {} has expired", url.getUrl());
            throw new UrlExpired("The url for this code is expired");
        }
        String fullUrl = url.getUrl();
        url.setLastAccessed(LocalDateTime.now());
        url.setClicksCount(url.getClicksCount() + 1);
        return fullUrl;
    }

}

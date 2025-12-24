package com.example.urlShortenerServer.service;

import com.example.urlShortenerServer.controller.UrlController;
import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.exceptions.UrlExpired;
import com.example.urlShortenerServer.exceptions.UrlNotFound;
import com.example.urlShortenerServer.repository.UrlRepository;
import jakarta.transaction.Transactional;
import org.hibernate.type.TrueFalseConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UrlService {
    private static final Logger log =
            LoggerFactory.getLogger(UrlService.class);

    private UrlRepository urlRepository;
    @Autowired
    public void setRepository(UrlRepository repository) {
        this.urlRepository = repository;
    }

    private String randomCodeGenerator(){
        UUID uuid = UUID.randomUUID();
        BigInteger value = BigInteger.valueOf(uuid.getMostSignificantBits())
                .shiftLeft(64)
                .or(BigInteger.valueOf(uuid.getLeastSignificantBits()).abs());

        return value.toString(62).substring(0, 8);
    }

    @Transactional//allows dirty checking, if I change the returned object after the db query, the db is updated as well
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

    @Transactional
    public String addShortenedUrl(String urlAddress){
        String randomCode;
        do {
            randomCode = randomCodeGenerator();
        } while (urlRepository.existsByShortUrl(randomCode));
        Url url = Url.builder()
                .url(urlAddress)
                .shortUrl(randomCode)
                .createdAt(LocalDateTime.now())
//                .expiration(LocalDateTime.now().plusMinutes(1))
                .expiration(LocalDateTime.now().plusDays(2))
                .lastAccessed(LocalDateTime.now())
                .clicksCount(0L)
                .active(true)
                .build();
        urlRepository.save(url);
        return randomCode;
    }

    public List<Url> getAllUrls(){
        return urlRepository.findAll();
    }

    public Url getUrlById(long id){
        Url url = urlRepository.findAllById(id);

        if (url == null){
            log.warn("The url with the id = {} is not found", id);
            throw new UrlNotFound("The wanted url is not found");
        }
        return url;
    }

    @Transactional
    @Scheduled(fixedDelay = 60000)
    public void deactivateExpired(){
        log.info("Cleanup job has started");
        List<Url> urls = urlRepository.findAllByActive(true);
        LocalDateTime now = LocalDateTime.now();
        for (var i:urls){
            if (i.getActive() && now.isAfter(i.getExpiration())){
                i.setActive(false);
                log.info("Deactivated the url with id = {}", i.getId());
            }
        }
    }

}

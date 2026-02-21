package com.example.urlShortenerServer.service;

import com.example.urlShortenerServer.controller.UrlController;
import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.domain.User;
import com.example.urlShortenerServer.domain.UserPrincipal;
import com.example.urlShortenerServer.exceptions.InexistentUser;
import com.example.urlShortenerServer.exceptions.Unauthorized;
import com.example.urlShortenerServer.exceptions.UrlExpired;
import com.example.urlShortenerServer.exceptions.UrlNotFound;
import com.example.urlShortenerServer.repository.AnalyticsRepository;
import com.example.urlShortenerServer.repository.UrlRepository;
import com.example.urlShortenerServer.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.type.TrueFalseConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

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
        return value.abs().toString(62).substring(0, 8);
    }

    @Transactional//allows dirty checking, if I change the returned object after the db query, the db is updated as well
    public Url findUrlByCode(String code) throws UrlExpired, UrlNotFound{
        Url url = urlRepository.findByShortUrl(code).orElseThrow(() -> {
            log.warn("The wanted url for the code = {} is not found", code);
            return new UrlNotFound("There is no url found in the database by that code");
        });

        if (!url.getActive() || LocalDateTime.now().isAfter(url.getExpiration())){
            url.setActive(false);
            log.warn("The wanted url = {} has expired", url.getUrl());
            throw new UrlExpired("The url for this code is expired");
        }
        url.setLastAccessed(LocalDateTime.now());
        url.setClicksCount(url.getClicksCount() + 1);
        return url;
    }

    @Transactional
    public String addShortenedUrl(String urlAddress){
        String randomCode;
        do {
            randomCode = randomCodeGenerator();
        } while (urlRepository.existsByShortUrl(randomCode));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
            throw new InexistentUser("The wanted user is not logged in");
        }

        User user = principal.getUser();

        Url url = Url.builder()
                .url(urlAddress)
                .shortUrl(randomCode)
                .createdAt(LocalDateTime.now())
//                .expiration(LocalDateTime.now().plusMinutes(1))
                .expiration(LocalDateTime.now().plusDays(2))
                .lastAccessed(LocalDateTime.now())
                .clicksCount(0L)
                .active(true)
                .user(user)
                .extensions(0)
                .build();
        urlRepository.save(url);
        return randomCode;
    }

    public List<Url> getAllUrls(){
        return urlRepository.findAll();
    }

    public Url getUrlById(long id, User user){
        Optional<Url> url = urlRepository.findById(id);
        Url exactUrl = url.orElseThrow(() -> new UrlNotFound("The wanted url is not found"));

        if (!Objects.equals(exactUrl.getUser().getId(), user.getId())){
            throw new Unauthorized("You are not authorized to do this");
        }

        return exactUrl;
    }

    @Transactional
    public void invalidateUrl(long urlId, User user){

        Url url = urlRepository.getUrlByIdAndActive(urlId, true);

        if(url == null){
            throw new UrlNotFound("The wanted url doesn't exist");
        }

        if (!Objects.equals(user.getId(), url.getUser().getId())){
            throw new Unauthorized("You are not authorized to do this");
        }

        url.setActive(false);
        url.setExpiration(LocalDateTime.now());
    }

    @Transactional
    public Url extendUrl(long id, User user, int days){

        Optional<Url> url = urlRepository.findById(id);
        Url exactUrl = url.orElseThrow(() -> new UrlNotFound("The wanted url is not found"));

        if (!Objects.equals(exactUrl.getUser().getId(), user.getId())){
            throw new Unauthorized("You are not authorized to do this");
        }

        if (exactUrl.getExtensions() > 5){
            throw new Unauthorized("You can't extend the url more times");
        }

        exactUrl.setExpiration(exactUrl.getExpiration().plusDays(days));
        exactUrl.setExtensions(exactUrl.getExtensions() + 1);
        return exactUrl;
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

    public List<Url> getUrlByUserId(long userId){
        return urlRepository.findAllByUser_Id(userId);
    }



}

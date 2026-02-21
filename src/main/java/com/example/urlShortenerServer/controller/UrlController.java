package com.example.urlShortenerServer.controller;


import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.domain.UserPrincipal;
import com.example.urlShortenerServer.dto.*;
import com.example.urlShortenerServer.enums.UserRole;
import com.example.urlShortenerServer.exceptions.InexistentUser;
import com.example.urlShortenerServer.exceptions.InvalidData;
import com.example.urlShortenerServer.exceptions.InvalidUrl;
import com.example.urlShortenerServer.exceptions.Unauthorized;
import com.example.urlShortenerServer.service.AnalyticsService;
import com.example.urlShortenerServer.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class UrlController {

    private static final Logger log =
            LoggerFactory.getLogger(UrlController.class);
    private UrlService urlService;

    @Autowired
    public void setUrlService(UrlService urlService) {
        this.urlService = urlService;
    }

    private AnalyticsService analyticsService;
    @Autowired
    public void setAnalyticsService(AnalyticsService analyticsService){
        this.analyticsService = analyticsService;
    }

    private static final int MAX_EXTENSIONS = 5;

    @GetMapping("/r/{code}")
    public ResponseEntity<?> redirectByCode(@PathVariable String code){
        log.info("Redirect request for code = {}", code);
        Url urlObj = urlService.findUrlByCode(code);

        String url = urlObj.getUrl();

        analyticsService.addAnalytic(urlObj);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody UrlRequest request) {
        log.info("Url shortening requested for url = {}", request.getUrlAddress());

        try {
            String fullUrl = request.getUrlAddress();

            String code = urlService.addShortenedUrl(fullUrl);
            UrlAddedResponse urlAddedResponse = UrlAddedResponse.builder().code(code).fullShortUrl("http://localhost:8080/r/" + code).build();
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(urlAddedResponse);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new InvalidUrl("The given url is invalid");
        }
    }

//    @GetMapping("/urls")
//    public ResponseEntity<?> getAllUrls(Authentication authentication){
//        log.info("Retreival for all urls has been requested");
//
//        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
//        if (userPrincipal == null){
//            throw new InexistentUser("You are not logged in");
//        }
//        if (userPrincipal.getUser().getRole() != UserRole.ADMIN){
//            throw new Unauthorized("You are not authorized to do this");
//        }
//
//        List<Url> urls = urlService.getAllUrls();
//        return ResponseEntity.ok().body(urls);
//    }

    @GetMapping("/urls/{id}")
    public ResponseEntity<?> getUrlById(@PathVariable long id, Authentication authentication){
        log.info("Retreival of url with id = {} has been requested", id);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("You are not logged in");
        }

        Url url = urlService.getUrlById(id, userPrincipal.getUser());
        UrlResponse urlResponse = new UrlResponse(
                url.getId(),
                url.getUrl(),
                url.getShortUrl(),
                url.getCreatedAt(),
                url.getExpiration(),
                url.getLastAccessed(),
                url.getClicksCount(),
                url.getActive(),
                url.getExtensions(),
                MAX_EXTENSIONS);

        return ResponseEntity.ok().body(urlResponse);
    }

    @PutMapping("/invalidate/{id}")
    public ResponseEntity<?> invalidateUrl(@PathVariable long id, Authentication authentication){
        log.info("The {} url is requested to be invalidated", id);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("You are not logged in");
        }

        urlService.invalidateUrl(id, userPrincipal.getUser());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/analytics/{id}")
    public ResponseEntity<?> analyticsUrl(@PathVariable long id, Authentication authentication){

        log.info("The url with id = {} is requested to be analysed", id);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("You are not logged in");
        }

        Url url = urlService.getUrlById(id, userPrincipal.getUser());

        return ResponseEntity.ok().body(analyticsService.getUrlAnalytics(url));
    }

    @PutMapping("/url/{id}/extend")
    public ResponseEntity<?> extendUrl(@PathVariable long id, Authentication authentication, @RequestBody ExtensionDto extensionDto){
        log.info("An extension for the url with id = {} was requested", id);
        Integer days = extensionDto.getDays();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("You are not logged in");
        }

        if (days <= 0){
            throw new InvalidData("The provided data is invalid");
        }

        Url url = urlService.extendUrl(id, userPrincipal.getUser(), days);

        UrlExtensionDto urlResponse = UrlExtensionDto.builder()
                .expiration(url.getExpiration())
                .id(url.getId())
                .extensions(url.getExtensions())
                .maximumExtensions(MAX_EXTENSIONS)
                .build();

        return ResponseEntity.ok().body(urlResponse);
    }
}

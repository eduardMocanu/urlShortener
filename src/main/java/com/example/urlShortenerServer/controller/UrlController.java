package com.example.urlShortenerServer.controller;


import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.dto.UrlAddedResponse;
import com.example.urlShortenerServer.dto.UrlRequest;
import com.example.urlShortenerServer.exceptions.InvalidUrl;
import com.example.urlShortenerServer.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class UrlController {

    private static final Logger log =
            LoggerFactory.getLogger(UrlController.class);
    private UrlService urlService;

    @Autowired
    public void setUrlService(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/r/{code}")
    public ResponseEntity<?> redirectByCode(@PathVariable String code){
        log.info("Redirect request for code = {}", code);
        String url = urlService.findUrlByCode(code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }

    @PostMapping("/urls")
    public ResponseEntity<?> shortenUrl(@RequestBody UrlRequest request) {
        log.info("Url shortening requested for url = {}", request.getUrlAddress());

        try {
            String fullUrl = "https://" + request.getUrlAddress();
            URI uri = new URI(fullUrl);

            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new InvalidUrl("The given url is invalid");
            }

            String code = urlService.addShortenedUrl(fullUrl);
            UrlAddedResponse urlAddedResponse = UrlAddedResponse.builder().code(code).fullShortUrl("http://localhost:8080/r/" + code).build();
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(urlAddedResponse);

        } catch (Exception e) {
            throw new InvalidUrl("The given url is invalid");
        }
    }

    @GetMapping("/urls")
    public ResponseEntity<?> getAllUrls(){

        log.info("Retreival for all urls has been requested");

        List<Url> urls = urlService.getAllUrls();
        return ResponseEntity.status(HttpStatus.OK).body(urls);
    }

    @GetMapping("/urls/{id}")
    public ResponseEntity<?> getUrlById(@PathVariable long id){

        log.info("Retreival of url with id = {} has been requested", id);

        Url url = urlService.getUrlById(id);

        return ResponseEntity.status(HttpStatus.OK).body(url);
    }
}

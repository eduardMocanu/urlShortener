package com.example.urlShortenerServer.controller;


import com.example.urlShortenerServer.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

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
    public ResponseEntity<?> redirectByCode(@RequestParam String code){
        String url = urlService.findUrlByCode(code);
        log.info("Redirect request for code = {}", code);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }


}

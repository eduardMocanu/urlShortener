package com.example.urlShortenerServer.controller;


import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.domain.UserPrincipal;
import com.example.urlShortenerServer.dto.UrlAddedResponse;
import com.example.urlShortenerServer.dto.UrlDto;
import com.example.urlShortenerServer.dto.UrlRequest;
import com.example.urlShortenerServer.enums.UserRole;
import com.example.urlShortenerServer.exceptions.InexistentUser;
import com.example.urlShortenerServer.exceptions.InvalidUrl;
import com.example.urlShortenerServer.exceptions.Unauthorized;
import com.example.urlShortenerServer.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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

    @GetMapping("/r/{code}")
    public ResponseEntity<?> redirectByCode(@PathVariable String code){
        log.info("Redirect request for code = {}", code);
        String url = urlService.findUrlByCode(code);
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

    @GetMapping("/urls")
    public ResponseEntity<?> getAllUrls(Authentication authentication){
        log.info("Retreival for all urls has been requested");

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("You are not logged in");
        }
        if (userPrincipal.getUser().getRole() != UserRole.ADMIN){
            throw new Unauthorized("You are not authorized to do this");
        }

        List<Url> urls = urlService.getAllUrls();
        return ResponseEntity.ok().body(urls);
    }

    @GetMapping("/urls/{id}")
    public ResponseEntity<?> getUrlById(@PathVariable long id, Authentication authentication){
        log.info("Retreival of url with id = {} has been requested", id);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("You are not logged in");
        }

        Url url = urlService.getUrlById(id, userPrincipal.getUser());
        UrlDto urlDto = new UrlDto(
                url.getId(),
                url.getUrl(),
                url.getShortUrl(),
                url.getCreatedAt(),
                url.getExpiration(),
                url.getLastAccessed(),
                url.getClicksCount(),
                url.getActive());

        return ResponseEntity.ok().body(urlDto);
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
}

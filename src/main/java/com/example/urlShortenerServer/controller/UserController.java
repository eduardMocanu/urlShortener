package com.example.urlShortenerServer.controller;

import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.domain.User;
import com.example.urlShortenerServer.domain.UserPrincipal;
import com.example.urlShortenerServer.dto.UrlDto;

import com.example.urlShortenerServer.dto.UserRequest;
import com.example.urlShortenerServer.dto.UserResponse;
import com.example.urlShortenerServer.exceptions.InexistentUser;
import com.example.urlShortenerServer.service.JwtService;
import com.example.urlShortenerServer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {
    private static final Logger log =
            LoggerFactory.getLogger(UserController.class);

    private UserService userService;
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest){
        log.info("Registration for username = {} and password = {} has been requested", userRequest.getUsername(), userRequest.getPassword());

        userRequest.setUsername(userRequest.getUsername().toLowerCase().strip());

        UserResponse userResponse = userService.registerUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserRequest userRequest){
        log.info("Login for username = {} and password = {} has been requested", userRequest.getUsername(), userRequest.getPassword());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        userRequest.getUsername(), userRequest.getPassword()
                )
        );
        String token = jwtService.generateToken(userRequest.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("token", token));
    }

    @GetMapping("/oauth/success")
    public ResponseEntity<?> loginOauth(Authentication authentication){
        log.info("Login using oauth has been initialized");

        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        if (user == null){
            throw new InexistentUser("The wanted user is not logged in");
        }
        String email = user.getAttribute("email");
        String token = jwtService.generateToken(email);
        //TO DO: redirect to the frontend page
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("token", token));
    }


    @GetMapping("/account")
    public ResponseEntity<?> getAccount(Authentication authentication){
        log.info("The details for an account have been requested");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("The wanted user is not logged in");
        }
        Long userId = userPrincipal.getUser().getId();
        List<UrlDto> urlsDto = userService.getAllUrlsOfUser(userId);
        return ResponseEntity.ok(urlsDto);
    }
}

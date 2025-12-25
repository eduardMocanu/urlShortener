package com.example.urlShortenerServer.controller;

import com.example.urlShortenerServer.dto.UserRequest;
import com.example.urlShortenerServer.dto.UserResponse;
import com.example.urlShortenerServer.service.UrlService;
import com.example.urlShortenerServer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private static final Logger log =
            LoggerFactory.getLogger(UserController.class);

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest){
        log.info("Registration for username = {} and password = {} has been requested", userRequest.getUsername(), userRequest.getPassword());
        userRequest.setUsername(userRequest.getUsername().toLowerCase());
        userRequest.setPassword(encoder.encode(userRequest.getPassword()));

        UserResponse userResponse = userService.registerUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

}

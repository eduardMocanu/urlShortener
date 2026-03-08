package com.example.urlShortenerServer.controller;

import com.example.urlShortenerServer.domain.UserPrincipal;
import com.example.urlShortenerServer.dto.UrlResponse;

import com.example.urlShortenerServer.dto.UserRequest;
import com.example.urlShortenerServer.dto.UserResponse;
import com.example.urlShortenerServer.exceptions.InexistentUser;
import com.example.urlShortenerServer.service.JwtService;
import com.example.urlShortenerServer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {
    private static final Logger log =
            LoggerFactory.getLogger(UserController.class);

    @Value("${frontend.url}")
    private String frontend_url;

    @Value("${cookie.domain}")
    private String cookieDomain;

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
        ResponseCookie responseCookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .domain(cookieDomain)
                .maxAge(60 * 60)
                .build();

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).build();

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
        ResponseCookie responseCookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .domain(cookieDomain)
                .maxAge(60 * 60)
                .build();

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .header(HttpHeaders.LOCATION, frontend_url)
                .build();
    }


    @GetMapping("/account")
    public ResponseEntity<?> getAccount(Authentication authentication){
        log.info("The details for an account have been requested");
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("The wanted user is not logged in");
        }
        Long userId = userPrincipal.getUser().getId();
        List<UrlResponse> urlsDto = userService.getAllUrlsOfUser(userId);
        return ResponseEntity.ok(urlsDto);
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> checkLoggedIn(Authentication authentication){
        log.info("The request to make sure that the cookie is valid has been made");

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        if (userPrincipal == null){
            throw new InexistentUser("The wanted user is not logged in");
        }

        return ResponseEntity.ok(Map.of("Code", "200"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response) {

        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear with domain (current format)
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .domain(cookieDomain)
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        // Clear without domain (legacy cookies set before domain was added)
        ResponseCookie cookieNoDomain = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookieNoDomain.toString());

        // Clear JSESSIONID
        ResponseCookie jsessionCookie = ResponseCookie.from("JSESSIONID", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, jsessionCookie.toString());

        return ResponseEntity.ok().build();
    }
}

package com.example.urlShortenerServer.service;


import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.domain.User;
import com.example.urlShortenerServer.dto.UrlResponse;
import com.example.urlShortenerServer.dto.UserRequest;
import com.example.urlShortenerServer.dto.UserResponse;
import com.example.urlShortenerServer.enums.UserRole;
import com.example.urlShortenerServer.exceptions.UsernameExistsAlready;
import com.example.urlShortenerServer.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    private UrlService urlService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);



    public UserResponse registerUser(UserRequest userRequest){

        String username = userRequest.getUsername();
        String passwordHash = encoder.encode(userRequest.getPassword());

        List<User> users = userRepository.findAllByUsername(username);
        if (!users.isEmpty()){
            log.warn("There is another user with the username = {}", username);
            throw new UsernameExistsAlready("The given username is already in the db");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordHash);
        user.setRole(UserRole.USER);

        User userQuery = userRepository.save(user);

        UserResponse userResponse = new UserResponse();
        userResponse.setId(userQuery.getId());
        userResponse.setUsername(userQuery.getUsername());
        userResponse.setRole(userQuery.getRole());

        return userResponse;
    }

    @Transactional
    public User findOrCreateOAuthGoogle(Map<String, Object> attributes){
        String authId = (String) attributes.get("sub");
        User user = userRepository.findByAuthId(authId);
        if (user == null){
            log.warn("The user is being created because it doesn't exist");
            user = new User();
            user.setRole(UserRole.USER);
            user.setAuthId(authId);
            user.setAuthProvider("GOOGLE");
            user.setUsername((String) attributes.get("name"));
            userRepository.save(user);
        }
        return user;
    }

    public List<UrlResponse> getAllUrlsOfUser(Long userId){


        List<Url> urls = urlService.getUrlByUserId(userId);
        List<UrlResponse> urlResponses = urls.stream()
                .map(u -> new UrlResponse(
                        u.getId(),
                        u.getUrl(),
                        u.getShortUrl(),
                        u.getCreatedAt(),
                        u.getExpiration(),
                        u.getLastAccessed(),
                        u.getClicksCount(),
                        u.getActive()
                ))
                .toList();
        return urlResponses;
    }

    public User getUserByUsername(String username){
        return userRepository.findByUsername(username);
    }
}

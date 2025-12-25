package com.example.urlShortenerServer.service;


import com.example.urlShortenerServer.controller.UserController;
import com.example.urlShortenerServer.domain.User;
import com.example.urlShortenerServer.dto.UserRequest;
import com.example.urlShortenerServer.dto.UserResponse;
import com.example.urlShortenerServer.enums.UserRole;
import com.example.urlShortenerServer.exceptions.UsernameExistsAlready;
import com.example.urlShortenerServer.repository.UserRepository;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private static final Logger log =
            LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;
    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public UserResponse registerUser(UserRequest userRequest){

        String username = userRequest.getUsername();
        String passwordHash = userRequest.getPassword();

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
}

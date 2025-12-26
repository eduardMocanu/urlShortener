package com.example.urlShortenerServer.service;


import com.example.urlShortenerServer.domain.User;
import com.example.urlShortenerServer.dto.UserRequest;
import com.example.urlShortenerServer.dto.UserResponse;
import com.example.urlShortenerServer.enums.UserRole;
import com.example.urlShortenerServer.exceptions.InexistentUser;
import com.example.urlShortenerServer.exceptions.UsernameExistsAlready;
import com.example.urlShortenerServer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private BCryptPasswordEncoder encoder;
    @Autowired
    public void setEncoder(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }



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

    public UserResponse loginUser(UserRequest userRequest) {

        String username = userRequest.getUsername();
        String password = userRequest.getPassword();

        User user = userRepository.findByUsername(username);

        if (user == null || !encoder.matches(password, user.getPassword())){
            log.warn("The given account credentials are not valid username = {}, password = {}", username, password);
            throw new InexistentUser("The wanted user is not in the db");
        }

        UserResponse response = new UserResponse();
        response.setRole(user.getRole());
        response.setId(user.getId());
        response.setUsername(user.getUsername());

        return response;

    }
}

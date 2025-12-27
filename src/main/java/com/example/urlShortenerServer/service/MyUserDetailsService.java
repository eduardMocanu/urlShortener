package com.example.urlShortenerServer.service;

import com.example.urlShortenerServer.domain.User;
import com.example.urlShortenerServer.domain.UserPrincipal;
import com.example.urlShortenerServer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    private static final Logger log =
            LoggerFactory.getLogger(MyUserDetailsService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null){
            log.warn("Username not found; username = {}", username);
            throw new UsernameNotFoundException("The wanted username is not found");
        }
        return new UserPrincipal(user);
    }
}

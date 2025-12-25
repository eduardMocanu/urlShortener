package com.example.urlShortenerServer.repository;

import com.example.urlShortenerServer.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findAllByUsername(String username);
}

package com.example.urlShortenerServer.repository;

import com.example.urlShortenerServer.domain.Url;
import com.example.urlShortenerServer.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortUrl(String shortUrl);
    boolean existsByShortUrl(String shortUrl);

    List<Url> findAllByActive(Boolean active);

    Url findAllById(Long id);

    List<Url> findAllByUser_Id(Long userId);

    Long user(User user);
}
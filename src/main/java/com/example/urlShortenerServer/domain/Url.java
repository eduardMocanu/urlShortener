package com.example.urlShortenerServer.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "urls")
@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false)
    private String url;

    @Column(unique = true)
    private String shortUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiration;

    @Column(nullable = false)
    private LocalDateTime lastAccessed;

    @Column(nullable = false)
    private Long clicksCount;

    @Column(nullable = false)
    private Boolean active;
}

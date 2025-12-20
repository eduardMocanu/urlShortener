package com.example.urlShortenerServer.domain;

import com.example.urlShortenerServer.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Table(name = "users")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String authProvider;

    @Column(unique = true)
    private String authId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<URL> urls;
}

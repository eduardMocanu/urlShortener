package com.example.urlShortenerServer.dto;

import com.example.urlShortenerServer.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private long id;
    private String username;
    private UserRole role;
}

package com.example.urlShortenerServer.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserPrincipal implements UserDetails, OAuth2User {

    private User user;
    private Map<String, Object> attributes;

    //constructor for the username + password authentication
    public UserPrincipal(User user){
        this.user = user;
        this.attributes = null;
    }

    //constructor for the OAuth authentication
    public UserPrincipal(User user, Map<String, Object> attributes){
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    //used for the login with the OAuth2  because security needs the raw data returned from the provider
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    //tells me what I am allowed to do with my authority
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );
    }

    @Override
    //this is an identifier for the auth process itself
    public String getName() {
        return user.getId().toString();
    }


    //---PASSWORD + USERNAME
    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }


    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

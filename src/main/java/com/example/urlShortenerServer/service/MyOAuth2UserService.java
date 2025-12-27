package com.example.urlShortenerServer.service;
import com.example.urlShortenerServer.domain.User;
import com.example.urlShortenerServer.domain.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


import java.util.Map;
@Service
public class MyOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
//OAuth2UserRequest - contains the data Spring needs so that it can call the provider and get the auth info
//OAuth2User - contains the returned data by the provider using the OAuth2UserRequest
    @Autowired
    private UserService userService;

    private final DefaultOAuth2UserService delegate =
            new DefaultOAuth2UserService();

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();
        User user = userService.findOrCreateOAuthGoogle(attributes);
        return new UserPrincipal(user, attributes);
    }
}

//package com.example.googledrivemerge.services;
//
//import com.example.googledrivemerge.pojo.MyUser;
//import com.example.googledrivemerge.pojo.MyUserData;
//import com.example.googledrivemerge.repository.MyUserRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
//import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
//import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
//import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class CustomOAuth2UserService extends DefaultOAuth2UserService {
//
//    @Autowired
//    private MyUserRepository userRepository;
//
//    @Override
//    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
//        OAuth2User user = super.loadUser(userRequest);
//        updateTokens(userRequest, user);
//        return user;
//    }
//
//    private void updateTokens(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
//        String accessToken = userRequest.getAccessToken().getTokenValue();
//        String refreshToken = userRequest.getRefreshToken().getTokenValue();
//        // Логика сохранения токенов в базу данных
//    }
//}
//
//

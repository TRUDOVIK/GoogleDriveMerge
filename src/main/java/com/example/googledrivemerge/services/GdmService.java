package com.example.googledrivemerge.services;

import com.example.googledrivemerge.pojo.MyUser;
import com.example.googledrivemerge.repository.MyUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GdmService {
    MyUserRepository repository;
    private PasswordEncoder passwordEncoder;

//    public void addGoogleTokens(String accessToken, String refreshToken) {
//        try {
//            MyUser user = repository.findByUsername(username).get();
//            user.setToken(token);
//            repository.save(user);
//        } catch (Exception e) {
//            System.out.println("Username not found");
//        }
//    }

    public void updateToken(String token, String username) {
        try {
            MyUser user = repository.findByUsername(username).get();
            user.setToken(token);
            repository.save(user);
        } catch (Exception e) {
            System.out.println("Username not found");
        }
    }

    public void addUser(MyUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles("ROLE_ADMIN");
        repository.save(user);
    }

}

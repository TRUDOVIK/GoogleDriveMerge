package com.example.googledrivemerge.controller;

import com.example.googledrivemerge.config.MyUserDetails;
import com.example.googledrivemerge.dto.FileDto;
import com.example.googledrivemerge.dto.FilesRequestDto;
import com.example.googledrivemerge.dto.LoginRequest;
import com.example.googledrivemerge.dto.MyUserDataDto;
import com.example.googledrivemerge.mapper.MyMapper;
import com.example.googledrivemerge.pojo.MyUser;
import com.example.googledrivemerge.repository.MyUserDataRepository;
import com.example.googledrivemerge.repository.MyUserRepository;
import com.example.googledrivemerge.services.GdmService;
import com.example.googledrivemerge.services.GoogleDriveService;
import com.example.googledrivemerge.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:8081")
public class GoogleDriveController {
    private GdmService service;
    private GoogleDriveService googleDriveService;
    private final JwtUtils jwtUtils;
    private PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private MyUserDataRepository userDataRepository;
    private MyUserRepository userRepository;

    @GetMapping("/Callback")
    @ResponseBody
    public ResponseEntity<String> handleOAuthPostLogin(@RequestParam String code, @AuthenticationPrincipal MyUserDetails user) {

        RestTemplate restTemplate = new RestTemplate();

        String clientId = "383424257075-b13hl88n94es1ag646n2kghk85gespdp.apps.googleusercontent.com";
        String clientSecret = "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";
        String redirectUri = "http://localhost:8080/Callback";

        String tokenUri = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("redirect_uri", redirectUri);
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();

        try {
            MyUserDataDto userData = mapper.readValue(response.getBody(), MyUserDataDto.class);
            userData.setUser(user.getUser());
            userDataRepository.save(MyMapper.INSTANCE.myUserDataDtoToMyUserData(userData));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping("/add-account")
    public RedirectView initiateAuth() {
        return new RedirectView("https://accounts.google.com/o/oauth2/auth?access_type=offline&response_type=code&client_id=383424257075-b13hl88n94es1ag646n2kghk85gespdp.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive%20profile&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2FCallback");
    }

    @PostMapping("/signin")
    @ResponseStatus(value = HttpStatus.OK)
    public String authUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        service.updateToken(jwt, loginRequest.username());

        return jwt;
    }

    @GetMapping("/get-username")
    @ResponseStatus(value = HttpStatus.OK)
    public String authUser(@AuthenticationPrincipal MyUserDetails user) {
        return user.getUsername();
    }

    @DeleteMapping("/del-files")
    public String deleteFiles(@RequestBody List<FileDto> request, @AuthenticationPrincipal MyUserDetails user) throws Exception {
        return googleDriveService.deleteFiles(request, user);
    }

    @PostMapping("/files")
    public List<FileDto> getFiles(@RequestBody FilesRequestDto request, @AuthenticationPrincipal MyUserDetails user) throws Exception {
        return googleDriveService.getFiles(request.getSearchQuery(), request.getNextPageToken(), request.getPageSize(), request.getSortOrder(), user, request.getOwner());
    }

    @PostMapping("/folder")
    public List<FileDto> getFolderFiles(@RequestBody FilesRequestDto request, @AuthenticationPrincipal MyUserDetails user) throws Exception {
        return googleDriveService.getFolderFiles(request.getSearchQuery(), request.getNextPageToken(), request.getParentFolder(), request.getPageSize(), request.getSortOrder(), user, request.getOwner());
    }

    @PostMapping("/signup")
    public String addUser(@RequestBody MyUser user) {
        service.addUser(user);
        return "User is saved";
    }
}

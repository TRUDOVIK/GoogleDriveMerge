package com.example.googledrivemerge.controller;

import com.example.googledrivemerge.config.MyUserDetails;
import com.example.googledrivemerge.dto.*;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", allowedHeaders = "*")
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
        return googleDriveService.handleOAuthPostLogin(code, user);
    }

    @GetMapping("/add-account")
    public String initiateAuth() {
        return googleDriveService.addAccount();
    }

    @PostMapping("/sign-in")
    @ResponseStatus(value = HttpStatus.OK)
    public JwtDto authUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        //service.updateToken(jwt, loginRequest.username());

        return new JwtDto(jwtUtils.generateJwtToken(authentication));
    }

    @GetMapping("/get-username")
    @ResponseStatus(value = HttpStatus.OK)
    public String authUser(@AuthenticationPrincipal MyUserDetails user) {
        return user.getUsername();
    }

    @GetMapping("/get-view-link")
    @ResponseStatus(value = HttpStatus.OK)
    public String authUser(FileDto file) {
        return String.format("https://drive.google.com/file/d/%s/view", file.getId());
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

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("files") MultipartFile[] files, String mimeType, @AuthenticationPrincipal MyUserDetails user) {
        try {
            for (var file : files) {
                byte[] fileData = file.getBytes();
                String fileName = file.getOriginalFilename();
                googleDriveService.uploadFileAsync(fileData, fileName, mimeType, user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error uploading file";
        }
        return "All files have been successfully uploaded";
    }

    @PostMapping("/sign-up")
    public String addUser(@RequestBody MyUser user) {
        service.addUser(user);
        return "User is saved";
    }
}

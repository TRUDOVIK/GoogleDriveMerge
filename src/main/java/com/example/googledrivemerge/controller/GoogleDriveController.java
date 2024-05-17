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
import com.example.googledrivemerge.util.MimeTypeUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
//@CrossOrigin(origins = "http://localhost:8081", allowCredentials = "true", allowedHeaders = "*")
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

    //Ручка для тестов с бэка
    @GetMapping("/add-access")
    @ResponseStatus(value = HttpStatus.OK)
    public String addAccess(@AuthenticationPrincipal MyUserDetails user) {
        MyUserDataDto userData = new MyUserDataDto("ya29.a0AXooCgv_JcRifzeRpXjUtzjSb7lFS_ASA3OEobvqP-bzzCaBjGLUSSAfEVRXVAdDAfWsJcGA2kjGzfA9y3e4HB50sjkxZArmzKXfTWA28s3u4P6GyMTMJYB0h5Sf0YAk0HWw8XQz8P6h5w6OHCtjHSQ2XYriMDQ8VDuwaCgYKAZsSARISFQHGX2Mi6a9OcSJnwQeKSQ5-Hvirfg0171", "");
        userData.setUser(user.getUser());
        userDataRepository.save(MyMapper.INSTANCE.myUserDataDtoToMyUserData(userData));
        return "ok";
    }

    @GetMapping("/add-account")
    @ResponseStatus(value = HttpStatus.OK)
    public UrlResponseDto initiateAuth() {
        return new UrlResponseDto(googleDriveService.addAccount());
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
    public UsernameResponseDto getUsername(@AuthenticationPrincipal MyUserDetails user) {
        return new UsernameResponseDto(user.getUsername());
    }

    @PostMapping("/get-view-link")
    @ResponseStatus(value = HttpStatus.OK)
    public UrlResponseDto getViewLink(@RequestBody FileDto file) {
        return new UrlResponseDto(String.format("https://drive.google.com/file/d/%s/view", file.getId()));
    }

    @PostMapping("/get-share-link")
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getShareLink(@RequestBody FileDto file, @AuthenticationPrincipal MyUserDetails user) throws IOException {
        try {
            googleDriveService.shareFile(file.getId(), file.getOwner(), user);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(new UrlResponseDto(String.format("https://drive.google.com/file/d/%s/view", file.getId())));
    }


    @DeleteMapping("/del-files")
    @ResponseStatus(value = HttpStatus.OK)
    public MessageResponseDto deleteFiles(@RequestBody List<FileDto> request, @AuthenticationPrincipal MyUserDetails user) throws Exception {
        return new MessageResponseDto(googleDriveService.deleteFiles(request, user));
    }

    @PostMapping("/files")
    public ResponseEntity<FilesResponseDto> getFiles(@RequestBody FilesRequestDto request, @AuthenticationPrincipal MyUserDetails user) throws Exception {
        return googleDriveService.getFiles(request.getSearchQuery(), request.getNextPageToken(), request.getPageSize(), request.getSortOrder(), user, request.getOwner());
    }

    @PostMapping("/folder")
    public ResponseEntity<FilesResponseDto> getFolderFiles(@RequestBody FilesRequestDto request, @AuthenticationPrincipal MyUserDetails user) throws Exception {
        return googleDriveService.getFolderFiles(request.getSearchQuery(), request.getNextPageToken(), request.getParentFolder(), request.getPageSize(), request.getSortOrder(), user, request.getOwner());
    }

//    @PostMapping("/download")
//    public String handleFileDownload(@RequestBody DownloadRequestDto request, @AuthenticationPrincipal MyUserDetails user) throws Exception {
//        return googleDriveService.downloadFile(request, user);
//    }

    @PostMapping("/downloadFiles")
    public void downloadFiles(@RequestBody List<DownloadRequestDto> requestDtos, @AuthenticationPrincipal MyUserDetails user, HttpServletResponse response) {
        try {
            for (var requestDto : requestDtos) {
                downloadFile(requestDto, user, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/downloadFile")
    public void downloadFile(@RequestBody DownloadRequestDto requestDto, @AuthenticationPrincipal MyUserDetails user, HttpServletResponse response) {
        try {
            CompletableFuture<InputStream> futureInputStream = googleDriveService.downloadFile(requestDto.getFileId(), user, requestDto.getOwner());

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",  "attachment; filename=\"" + requestDto.getName() + "\"");

            InputStream inputStream = futureInputStream.get();

            OutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload")
    public MessageResponseDto handleFileUpload(@RequestParam("files") MultipartFile[] files, @RequestBody UploadFilesMetadataDto uploadFilesMetadataDto, @AuthenticationPrincipal MyUserDetails user) {
        try {
            for (var file : files) {
                byte[] fileData = file.getBytes();
                String fileName = file.getOriginalFilename();
                String mimeType = MimeTypeUtil.getMimeType(fileName);
                googleDriveService.uploadFileAsync(fileData, fileName, mimeType, uploadFilesMetadataDto.getFileId(), uploadFilesMetadataDto.getOwner(), user);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new MessageResponseDto("Error uploading file");
        }
        return new MessageResponseDto("All files have been successfully uploaded");
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> addUser(@RequestBody MyUser user) {
        try {
            service.addUser(user);
            return ResponseEntity.ok(authUser(new LoginRequest(user.getUsername(), user.getPassword())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}

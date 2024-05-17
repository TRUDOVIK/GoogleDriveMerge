package com.example.googledrivemerge.services;

import com.example.googledrivemerge.config.MyUserDetails;
import com.example.googledrivemerge.dto.FileDto;
import com.example.googledrivemerge.dto.FilesResponseDto;
import com.example.googledrivemerge.dto.MyUserDataDto;
import com.example.googledrivemerge.mapper.MyMapper;
import com.example.googledrivemerge.pojo.MyUserData;
import com.example.googledrivemerge.repository.MyUserDataRepository;
import com.example.googledrivemerge.repository.MyUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class GoogleDriveService {
    @Autowired
    private MyUserRepository userRepository;
    @Autowired
    private MyUserDataRepository userDataRepository;

    @Value("${client-id}")
    String clientId;
    @Value("${client-secret}")
    String clientSecret;
    @Value("${redirect-uri}")
    String redirectUri;


    public Drive checkAndRefreshToken(Drive service, MyUserDetails user, int owner) throws IOException {
        if (service == null || service.getRequestFactory() == null) {
            GoogleRefreshTokenRequest request = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(), new GsonFactory(), user.getUser().getMyUserData().get(owner).getRefreshToken(), clientId, clientSecret);

            TokenResponse tokenResponse = request.execute();
            String newAccessToken = tokenResponse.getAccessToken();

            service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                    request1 -> request1.getHeaders().setAuthorization("Bearer " + newAccessToken)).build();
            MyUserData userData = user.getUser().getMyUserData().get(owner);
            userData.setAccessToken(newAccessToken);
            userDataRepository.save(userData);
            return service;
        }
        return service;
    }

    public String addAccount() {
        return String.format("https://accounts.google.com/o/oauth2/auth?access_type=offline&response_type=code&client_id=%s&scope=https%%3A%%2F%%2Fwww.googleapis.com%%2Fauth%%2Fdrive%%20profile&redirect_uri=%s", clientId, redirectUri);
    }

    public ResponseEntity<String> handleOAuthPostLogin(@RequestParam String code, @AuthenticationPrincipal MyUserDetails user) {

        RestTemplate restTemplate = new RestTemplate();

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


    public ResponseEntity<FilesResponseDto> getFiles(String searchQuery, String pageToken, int pageSize, String sortOrder, MyUserDetails user, int owner) throws Exception {

        Integer nextOwnerIndex = owner;

        Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(owner).getAccessToken())).build();

        service = checkAndRefreshToken(service, user, owner);

        List<FileDto> files = new ArrayList<FileDto>();

        FileList result = service.files().list()
                    .setQ("'root' in parents and name contains '" + searchQuery + "'")
                    .setPageSize(pageSize)
                    .setOrderBy(sortOrder)
                    .setPageToken(pageToken)
                    .setFields("nextPageToken, files(id, name, thumbnailLink, webContentLink, iconLink, mimeType, parents)")
                    .setCorpora("user").execute();
        try {
            for (var file : result.getFiles()) {
                var currentFile = MyMapper.INSTANCE.fileToFileDto(file);
                currentFile.setOwner(owner);
                files.add(currentFile);
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }

        if ((result.getNextPageToken() == null || result.getNextPageToken().isEmpty())) {
            nextOwnerIndex++;
            if (nextOwnerIndex >= user.getUser().getMyUserData().size()) {
                nextOwnerIndex = null;
            }
        }
        //files.addAll(result.getFiles());
        //files.get(0).
        return ResponseEntity.ok(new FilesResponseDto(files, result.getNextPageToken(), nextOwnerIndex));
    }

    public ResponseEntity<FilesResponseDto> getFolderFiles(String searchQuery, String pageToken, String parentFolder, int pageSize, String sortOrder, MyUserDetails user, int owner) throws Exception {

        Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(owner).getAccessToken())).build();

        service = checkAndRefreshToken(service, user, owner);

        List<FileDto> files = new ArrayList<FileDto>();

        String qParam;
        if (searchQuery.isEmpty()) {
            qParam = "'";
        } else {
            qParam = "name contains '" + searchQuery + "' and '";
        }

        FileList result = service.files().list()
                .setQ(qParam + parentFolder + "' in parents")
                .setPageSize(pageSize)
                .setOrderBy(sortOrder)
                .setPageToken(pageToken)
                .setFields("nextPageToken, files(id, name, thumbnailLink, webContentLink, iconLink, mimeType, parents)")
                .setCorpora("user").execute();

        try {
            for (var file : result.getFiles()) {
                files.add(MyMapper.INSTANCE.fileToFileDto(file));
            }
        } catch (Exception e) {
            return ResponseEntity.status(400).build();
        }

        //files.addAll(result.getFiles());
        //files.get(0).
        return ResponseEntity.ok(new FilesResponseDto(files, result.getNextPageToken(), owner)) ;
    }

    public String deleteFiles(List<FileDto> files, MyUserDetails user) throws Exception {
        int lastOwner = files.get(0).getOwner();

        Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(files.get(0).getOwner()).getAccessToken())).build();

        service = checkAndRefreshToken(service, user, lastOwner);

        for (var file : files) {
            if (file.getOwner() > lastOwner) {
                service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                        request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(file.getOwner()).getAccessToken())).build();
                service = checkAndRefreshToken(service, user, file.getOwner());
                lastOwner = file.getOwner();
            }
            try {
                service.files().delete(file.getId()).execute();
                System.out.println("File with ID " + file.getId() + " has been deleted successfully.");
            } catch (Exception e) {
                System.out.println(String.format("An error occurred while deleting file %s: ", file.getId()) + e.getMessage());
            }
        }

        return "All files were successfully deleted";
    }


    @Async
    public CompletableFuture<String> uploadFileAsync(byte[] fileData, String fileName, String mimeType, MyUserDetails user) {
        try {
            File fileMetadata = new File();
            fileMetadata.setName(fileName);

            Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                    request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(0).getAccessToken())).build();

            service = checkAndRefreshToken(service, user, 0);

            ByteArrayContent mediaContent = new ByteArrayContent(mimeType, fileData);

            File file = service.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();

            return CompletableFuture.completedFuture(file.getId());
        } catch (Exception e) {
            CompletableFuture<String> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
        }
    }

    @Async
    public CompletableFuture<InputStream> downloadFile(String fileId, MyUserDetails user, int owner) throws IOException {

        Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(owner).getAccessToken())).build();

        service = checkAndRefreshToken(service, user, owner);

        return CompletableFuture.completedFuture(service.files().get(fileId).setAlt("media").executeMediaAsInputStream());
    }

    public void shareFile(String fileId, int owner, MyUserDetails user) throws IOException {
        Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(owner).getAccessToken())).build();

        service = checkAndRefreshToken(service, user, owner);
        Permission permission = new Permission()
                .setType("anyone")
                .setRole("reader");
        service.permissions().create(fileId, permission).execute();
    }

//    public String downloadFile(DownloadRequestDto requestDto, MyUserDetails user) throws Exception {
//        URL url = new URL(requestDto.getWebContentLink());
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("Authorization", "Bearer " + user.getUser().getMyUserData().get(requestDto.getOwner()).getAccessToken());
//
//        int responseCode = connection.getResponseCode();
//        if (responseCode == HttpURLConnection.HTTP_OK) {
//            return connection.getURL().toString();
//        } else {
//            throw new IOException("Failed to get direct download link. Response code: " + responseCode);
//        }
//    }
}


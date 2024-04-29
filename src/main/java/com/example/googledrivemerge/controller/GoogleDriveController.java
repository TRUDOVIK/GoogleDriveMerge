package com.example.googledrivemerge.controller;

import com.example.googledrivemerge.GoogleDriveMergeApplication;
import com.example.googledrivemerge.config.MyUserDetails;
import com.example.googledrivemerge.dto.LoginRequest;
import com.example.googledrivemerge.pojo.MyUser;
import com.example.googledrivemerge.services.GdmService;
import com.example.googledrivemerge.util.JwtUtils;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@AllArgsConstructor
public class GoogleDriveController {
    private GdmService service;
    private final JwtUtils jwtUtils;
    private PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Application name.
     */
    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    /**
     * Directory to store authorization tokens for this application.
     */
    private static final String TOKENS_DIRECTORY_PATH = "tokens";



    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES =
            Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    @Autowired
    private final OAuth2AuthorizedClientService authorizedClientService;



//    @GetMapping("/google/login")
//    public RedirectView googleLogin(OAuth2AuthenticationToken authentication) {
//        String registrationId = "google"; // Идентификатор, заданный в конфигурации Spring Security
//        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId(registrationId)
//                .principal(authentication)
//                .build();
//        OAuth2AuthorizedClient authorizedClient = this.authorizedClientService.authorize(authorizeRequest);
//        if (authorizedClient == null) {
//            throw new IllegalArgumentException("Cannot authorize with Google");
//        }
//
//        return new RedirectView("/api/auth/google/callback");
//    }

    //@Operation(summary = "Получить список серверов", description = "Возвращает список всех серверов")
//    @GetMapping("${username}/files")
//    public FileList getFiles(@Valid GetServerParam getServerParam) {
//
//        Date startDate = null;
//        Date endDate = null;
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        try {
//            if (getServerParam.getStartDateStr() != null) {
//                startDate = dateFormat.parse(getServerParam.getStartDateStr());
//            }
//
//            if (getServerParam.getEndDateStr() != null) {
//                endDate = dateFormat.parse(getServerParam.getEndDateStr());
//            }
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//        }
//
//        Sort.Direction sortDirection = Sort.Direction.fromString(getServerParam.getDirection());
//
//        Pageable pageable = PageRequest.of(getServerParam.getPage(), getServerParam.getSize(), sortDirection, getServerParam.getField());
//        Specification<Server> spec = serverSpecification.filterByCriteria(startDate, endDate, getServerParam.getName(), getServerParam.getStatus(), getServerParam.getType(), getServerParam.getIndicator());
//
//        Page<Server> serverPage = serverRepository.findAll(spec, pageable);
//
//        long totalElements = serverPage.getTotalElements();
//        var response = serverMapper.serverPageToServerResponseDtoList(serverPage);
//        return new ServerPageResponseDTO(response, totalElements);
//    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT)
            throws IOException {
        // Load client secrets.
        InputStream in = GoogleDriveMergeApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        return credential;
    }

    private static GoogleAuthorizationCodeFlow getFlow(NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStreamReader reader = new InputStreamReader(GoogleDriveController.class.getResourceAsStream(CREDENTIALS_FILE_PATH));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
        return new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
    }
    @GetMapping("/get-token")
    public void getToken(@RequestParam("code") String authorizationCode, @AuthenticationPrincipal MyUserDetails user) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        GoogleAuthorizationCodeFlow flow = getFlow(HTTP_TRANSPORT);
        GoogleAuthorizationCodeRequestUrl url = flow.newAuthorizationUrl();
        String redirectUri = "http://localhost:8080/oauth2callback";
        String authorizationUrl = url.setRedirectUri(redirectUri).build();
        System.out.println("Перейдите по следующей ссылке для авторизации:");
        System.out.println(authorizationUrl);
                TokenResponse tokenResponse = flow.newTokenRequest(authorizationCode)
                .setRedirectUri(redirectUri)
                .execute();
        String accessToken = tokenResponse.getAccessToken();
        String refreshToken = tokenResponse.getRefreshToken();
        Credential credential = new GoogleCredential().setAccessToken(accessToken).setRefreshToken(refreshToken);
        System.err.println(accessToken);




        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    //TODO добавить @AuthenticationPrincipal MyUserDetails user
    @GetMapping("/Callback")
    @ResponseBody
    public ResponseEntity<String> handleOAuthPostLogin(@RequestParam String code) {

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

        return response;
    }

    @GetMapping("/add-account")
    public RedirectView initiateAuth() {
        return new RedirectView("https://accounts.google.com/o/oauth2/auth?access_type=offline&response_type=code&client_id=383424257075-b13hl88n94es1ag646n2kghk85gespdp.apps.googleusercontent.com&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fdrive&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2FCallback");
    }

    @GetMapping("/save-tokens")
    public String saveTokens() {
        return "успех";
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

    @PostMapping("/register")
    public String addUser(@RequestBody MyUser user) {
        service.addUser(user);
        return "User is saved";
    }
}

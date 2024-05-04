package com.example.googledrivemerge.services;

import com.example.googledrivemerge.config.MyUserDetails;
import com.example.googledrivemerge.dto.FileDto;
import com.example.googledrivemerge.dto.MyUserDataDto;
import com.example.googledrivemerge.mapper.MyMapper;
import com.example.googledrivemerge.pojo.MyUser;
import com.example.googledrivemerge.repository.MyUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleDriveService {
    @Autowired
    private MyUserRepository userRepository;

    public List<FileDto> getFiles(String searchQuery, String pageToken, int pageSize, String sortOrder, MyUserDetails user, int owner) throws Exception {

        Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(owner).getAccessToken())).build();


        List<FileDto> files = new ArrayList<FileDto>();

        FileList result = service.files().list()
                    .setQ("name contains '" + searchQuery)
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
            e.printStackTrace();
        }

        //files.addAll(result.getFiles());
        //files.get(0).
        return files;
    }

    public List<FileDto> getFolderFiles(String searchQuery, String pageToken, String parentFolder, int pageSize, String sortOrder, MyUserDetails user, int owner) throws Exception {

        Drive service = new Drive.Builder(new NetHttpTransport(), new GsonFactory(),
                request -> request.getHeaders().setAuthorization("Bearer " + user.getUser().getMyUserData().get(owner).getAccessToken())).build();


        List<FileDto> files = new ArrayList<FileDto>();

        FileList result = service.files().list()
                .setQ("name contains '" + searchQuery + "' and '" + parentFolder + "' in parents")
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
            e.printStackTrace();
        }

        //files.addAll(result.getFiles());
        //files.get(0).
        return files;
    }
}


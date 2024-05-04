package com.example.googledrivemerge.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@AllArgsConstructor
@Getter
@Setter
public class FilesRequestDto {
    private String searchQuery = "";
    private String nextPageToken = "";
    private String parentFolder = "root";
    private int pageSize = 10;
    private String sortOrder = "";
    private int owner = 0;

    public FilesRequestDto() {
    }
}
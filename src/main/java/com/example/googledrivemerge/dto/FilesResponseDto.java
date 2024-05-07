package com.example.googledrivemerge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class FilesResponseDto {
    private List<FileDto> fileDtos;
    private String nextPageToken = null;
    private Integer nextOwnerIndex = 0;
}

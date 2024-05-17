package com.example.googledrivemerge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class UploadFilesMetadataDto {
    String fileId;
    int owner = 0;
}

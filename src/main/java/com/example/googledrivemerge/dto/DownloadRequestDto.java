package com.example.googledrivemerge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class DownloadRequestDto {
    private String name = "unnamed";
    private int owner = 0;
    private String id;
}

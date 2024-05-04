package com.example.googledrivemerge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class FileDto {
    private String id;
    private String name;
    private String thumbnailLink;
    private String webContentLink;
    private String iconLink;
    private String mimeType;
    private String parents;
    private int owner;

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setThumbnailLink(String thumbnailLink) {
        this.thumbnailLink = thumbnailLink;
    }

    public void setWebContentLink(String webContentLink) {
        this.webContentLink = webContentLink;
    }

    public void setIconLink(String iconLink) {
        this.iconLink = iconLink;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public FileDto() {
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }
}

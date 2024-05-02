package com.example.googledrivemerge.mapper;

import com.example.googledrivemerge.dto.FileDto;
import com.example.googledrivemerge.dto.MyUserDataDto;
import com.example.googledrivemerge.pojo.MyUserData;
import com.google.api.services.drive.model.File;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MyMapper {
    MyMapper INSTANCE = Mappers.getMapper(MyMapper.class);

    MyUserData myUserDataDtoToMyUserData(MyUserDataDto dto);
    MyUserDataDto MyUserDataToMyUserDataDto(MyUserData entity);


    @Mapping(source = "parents", target = "parents", qualifiedByName = "formatParents")
    @Mapping(source = "id", target = "id")
    FileDto fileToFileDto(File file);

    @Named("formatParents")
    default String formatParents(List<String> parents) {
        return parents.get(0);
    }
}

package com.example.googledrivemerge.mapper;

import com.example.googledrivemerge.dto.MyUserDataDto;
import com.example.googledrivemerge.pojo.MyUserData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.factory.Mappers;

@Mapper
public interface MyMapper {
    MyMapper INSTANCE = Mappers.getMapper(MyMapper.class);

    MyUserData myUserDataDtoToMyUserData(MyUserDataDto dto);
    MyUserDataDto MyUserDataToMyUserDataDto(MyUserData entity);
}

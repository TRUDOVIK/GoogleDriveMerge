package com.example.googledrivemerge.dto;

import com.example.googledrivemerge.pojo.MyUser;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link MyUser}
 */
@Value
public class MyUserDto implements Serializable {
    Long id;
    String username;
    String password;
    String roles;

}
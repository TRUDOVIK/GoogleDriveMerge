package com.example.googledrivemerge.dto;

import com.example.googledrivemerge.pojo.MyUser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.example.googledrivemerge.pojo.MyUserData}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
public final class MyUserDataDto implements Serializable {
    @Getter
    private final LocalDate tokenUpdateTime = LocalDate.now();

    private final String accessToken;

    private final String refreshToken;

    @Setter
    private MyUser user;

    @JsonCreator
    public MyUserDataDto(@JsonProperty("access_token") String accessToken, @JsonProperty("refresh_token") String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public MyUser getUser() {
        return this.user;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof MyUserDataDto)) return false;
        final MyUserDataDto other = (MyUserDataDto) o;
        final Object this$accessToken = this.getAccessToken();
        final Object other$accessToken = other.getAccessToken();
        if (this$accessToken == null ? other$accessToken != null : !this$accessToken.equals(other$accessToken))
            return false;
        final Object this$refreshToken = this.getRefreshToken();
        final Object other$refreshToken = other.getRefreshToken();
        if (this$refreshToken == null ? other$refreshToken != null : !this$refreshToken.equals(other$refreshToken))
            return false;
        final Object this$user = this.getUser();
        final Object other$user = other.getUser();
        if (this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $accessToken = this.getAccessToken();
        result = result * PRIME + ($accessToken == null ? 43 : $accessToken.hashCode());
        final Object $refreshToken = this.getRefreshToken();
        result = result * PRIME + ($refreshToken == null ? 43 : $refreshToken.hashCode());
        final Object $user = this.getUser();
        result = result * PRIME + ($user == null ? 43 : $user.hashCode());
        return result;
    }

    public String toString() {
        return "MyUserDataDto(accessToken=" + this.getAccessToken() + ", refreshToken=" + this.getRefreshToken() + ", user=" + this.getUser() + ")";
    }
}
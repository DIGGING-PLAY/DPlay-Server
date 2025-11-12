package org.dplay.server.domain.auth.dto;

public record SocialUserDto(String platformId) {

    public static SocialUserDto of(String platformId) {
        return new SocialUserDto(platformId);
    }
}

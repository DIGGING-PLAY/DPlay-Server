package org.dplay.server.controller.auth.dto;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record JwtTokenResponse(
        Long userId,
        String accessToken,
        String refreshToken
) {
    public static JwtTokenResponse of(Long userId, String accessToken, String refreshToken) {
        return JwtTokenResponse.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}

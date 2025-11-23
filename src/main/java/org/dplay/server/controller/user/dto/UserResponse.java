package org.dplay.server.controller.user.dto;

import org.dplay.server.domain.user.dto.UserDetailResultDto;

public record UserResponse(
        Long userId,
        String nickname,
        String profileImg
) {

    public static UserResponse from(UserDetailResultDto resultDto) {
        return new UserResponse(resultDto.userId(), resultDto.nickname(), resultDto.profileImg());
    }
}

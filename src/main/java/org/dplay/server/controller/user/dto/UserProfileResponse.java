package org.dplay.server.controller.user.dto;

import org.dplay.server.domain.user.dto.UserDetailResultDto;
import org.dplay.server.domain.user.dto.UserProfileDto;

public record UserProfileResponse(
        UserResponse user,
        boolean isHost,
        boolean pushOn,
        Long postTotalCount
) {
    public static UserProfileResponse from(UserProfileDto userProfileDto) {
        return new UserProfileResponse(
                UserResponse.from(userProfileDto.user()),
                userProfileDto.isHost(),
                userProfileDto.pushOn(),
                userProfileDto.postTotalCount()
        );
    }
}

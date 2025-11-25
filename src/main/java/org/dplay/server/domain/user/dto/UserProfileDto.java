package org.dplay.server.domain.user.dto;

import lombok.Builder;

@Builder
public record UserProfileDto(
        UserDetailResultDto user,
        Boolean isHost,
        Boolean pushOn,
        Long postTotalCount
) {
    public static UserProfileDto of(
            UserDetailResultDto userDetailResultDto,
            Boolean isHost,
            Boolean pushOn,
            Long postTotalCount
    ) {
        return new UserProfileDto(
                userDetailResultDto,
                isHost,
                pushOn,
                postTotalCount
        );
    }
}
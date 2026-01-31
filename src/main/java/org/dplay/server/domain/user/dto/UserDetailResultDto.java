package org.dplay.server.domain.user.dto;

import org.dplay.server.domain.user.entity.User;

public record UserDetailResultDto(
        Long userId,
        String nickname,
        String profileImg,
        boolean isAdmin
) {
    public static UserDetailResultDto from(User user) {
        return new UserDetailResultDto(user.getUserId(), user.getNickname(), user.getProfileImg(), user.isEditor());
    }
}

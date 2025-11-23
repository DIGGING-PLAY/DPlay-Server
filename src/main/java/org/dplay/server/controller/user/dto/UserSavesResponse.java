package org.dplay.server.controller.user.dto;

import org.dplay.server.domain.post.dto.UserPostsResultDto;

import java.util.List;

public record UserSavesResponse(
        int visibleLimit,
        long totalCount,
        String nextCursor,
        List<UserSaveItemResponse> items
) {
    public static UserSavesResponse from(UserPostsResultDto resultDto) {
        return new UserSavesResponse(
                resultDto.visibleLimit(),
                resultDto.totalCount(),
                resultDto.nextCursor(),
                resultDto.items().stream()
                        .map(UserSaveItemResponse::from)
                        .toList()
        );
    }
}

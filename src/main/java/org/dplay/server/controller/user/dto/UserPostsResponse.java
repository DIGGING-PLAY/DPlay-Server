package org.dplay.server.controller.user.dto;

import org.dplay.server.domain.post.dto.UserPostsResultDto;

import java.util.List;

public record UserPostsResponse(
        int visibleLimit,
        long totalCount,
        String nextCursor,
        boolean isHost,
        List<UserPostItemResponse> items
) {
    public static UserPostsResponse from(UserPostsResultDto resultDto, boolean isHost) {
        return new UserPostsResponse(
                resultDto.visibleLimit(),
                resultDto.totalCount(),
                resultDto.nextCursor(),
                isHost,
                resultDto.items().stream()
                        .map(UserPostItemResponse::from)
                        .toList()
        );
    }
}


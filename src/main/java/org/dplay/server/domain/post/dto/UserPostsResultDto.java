package org.dplay.server.domain.post.dto;

import org.dplay.server.domain.post.entity.Post;

import java.util.List;

public record UserPostsResultDto(
        int visibleLimit,
        long totalCount,
        String nextCursor,
        List<Post> items
) {
    public static UserPostsResultDto from(
            int visibleLimit,
            long totalCount,
            String nextCursor,
            List<Post> items
    ) {
        return new UserPostsResultDto(
                visibleLimit, totalCount, nextCursor, items
        );
    }
}

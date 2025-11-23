package org.dplay.server.domain.post.dto;

import org.dplay.server.domain.post.entity.Post;

import java.util.List;

public record UserPostsResultDto(
        int visibleLimit,
        long totalCount,
        String nextCursor,
        List<Post> items
) {
}

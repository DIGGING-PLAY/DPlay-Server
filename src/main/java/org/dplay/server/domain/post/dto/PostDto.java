package org.dplay.server.domain.post.dto;

import org.dplay.server.domain.post.entity.Post;

public record PostDto(
        long postId
) {
    public static PostDto of(Post post) {
        return new PostDto(post.getPostId());
    }
}

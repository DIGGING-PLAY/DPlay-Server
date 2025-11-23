package org.dplay.server.domain.post.dto;

import org.dplay.server.domain.post.entity.Post;

public record PostLikeDto(
        long likeCount
) {
    public static PostLikeDto of(Post post) {
        return new PostLikeDto(post.getLikeCount());
    }
}

package org.dplay.server.domain.post.dto;

import org.dplay.server.domain.post.entity.Post;

public record PostLikeResultDto(
        boolean isLiked,
        int likeCount
) {
    public static PostLikeResultDto of(boolean isLiked, Post post) {
        return new PostLikeResultDto(isLiked, post.getLikeCount());
    }
}

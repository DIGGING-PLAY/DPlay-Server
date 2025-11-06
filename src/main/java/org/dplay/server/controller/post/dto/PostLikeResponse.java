package org.dplay.server.controller.post.dto;

public record PostLikeResponse(
        long likeCount
) {
    public static PostLikeResponse of(final long likeCount) {
        return new PostLikeResponse(likeCount);
    }
}

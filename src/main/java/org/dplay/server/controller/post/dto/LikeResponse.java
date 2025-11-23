package org.dplay.server.controller.post.dto;

public record LikeResponse(
        Boolean isLiked,
        int count
) {
    public static LikeResponse of(Boolean isLiked, int count) {
        return new LikeResponse(isLiked, count);
    }
}

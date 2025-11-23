package org.dplay.server.controller.post.dto;

public record PostIdResponse(
        long postId
) {
    public static PostIdResponse of(long postId) {
        return new PostIdResponse(postId);
    }
}


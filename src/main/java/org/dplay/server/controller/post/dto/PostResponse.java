package org.dplay.server.controller.post.dto;

public record PostResponse(
        long postId
) {
    public static PostResponse of(long postId) {
        return new PostResponse(postId);
    }
}


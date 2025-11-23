package org.dplay.server.controller.user.dto;

import org.dplay.server.controller.track.dto.UserTrackResponse;
import org.dplay.server.domain.post.entity.Post;

public record UserPostItemResponse(
        long postId,
        UserTrackResponse track,
        String content
) {
    public static UserPostItemResponse from(Post post) {
        return new UserPostItemResponse(
                post.getPostId(),
                UserTrackResponse.from(post.getTrack()),
                post.getContent()
        );
    }
}

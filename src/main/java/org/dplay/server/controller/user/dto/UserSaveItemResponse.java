package org.dplay.server.controller.user.dto;

import org.dplay.server.controller.track.dto.UserTrackResponse;
import org.dplay.server.domain.post.entity.Post;

public record UserSaveItemResponse(
        long postId,
        UserTrackResponse track
) {
    public static UserSaveItemResponse from(Post post) {
        return new UserSaveItemResponse(
                post.getPostId(),
                UserTrackResponse.from(post.getTrack())
        );
    }
}

package org.dplay.server.controller.user.dto;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.track.entity.Track;

public record UserPostItemResponse(
        long postId,
        UserPostTrackResponse track,
        String content
) {
    public static UserPostItemResponse from(Post post) {
        Track trackEntity = post.getTrack();
        return new UserPostItemResponse(
                post.getPostId(),
                new UserPostTrackResponse(
                        trackEntity.getTrackId(),
                        trackEntity.getSongTitle(),
                        trackEntity.getCoverImg(),
                        trackEntity.getArtistName()
                ),
                post.getContent()
        );
    }

    public record UserPostTrackResponse(
            String trackId,
            String songTitle,
            String coverImg,
            String artistName
    ) {
    }
}

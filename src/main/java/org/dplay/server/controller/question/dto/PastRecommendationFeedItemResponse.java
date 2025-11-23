package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.post.dto.PostFeedItemDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.user.entity.User;

public record PastRecommendationFeedItemResponse(
        long postId,
        boolean isEditorPick,
        boolean isScrapped,
        String content,
        PastRecommendationFeedTrack track,
        PastRecommendationFeedUser user,
        PastRecommendationFeedLike like
) {

    static PastRecommendationFeedItemResponse from(PostFeedItemDto dto) {
        Post post = dto.post();
        Track track = post.getTrack();
        User author = post.getUser();

        PastRecommendationFeedTrack feedTrack = new PastRecommendationFeedTrack(
                track.getTrackId(),
                track.getSongTitle(),
                track.getCoverImg(),
                track.getArtistName()
        );

        PastRecommendationFeedUser feedUser = new PastRecommendationFeedUser(
                author.getUserId(),
                author.getNickname(),
                author.getProfileImg()
        );

        PastRecommendationFeedLike feedLike = new PastRecommendationFeedLike(
                dto.isLiked(),
                post.getLikeCount()
        );

        return new PastRecommendationFeedItemResponse(
                post.getPostId(),
                dto.isEditorPick(),
                dto.isScrapped(),
                post.getContent(),
                feedTrack,
                feedUser,
                feedLike
        );
    }
}

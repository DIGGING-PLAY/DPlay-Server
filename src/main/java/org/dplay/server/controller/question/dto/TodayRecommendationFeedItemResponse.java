package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.post.dto.PostFeedItemDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.user.entity.User;

public record TodayRecommendationFeedItemResponse(
        long postId,
        boolean isScrapped,
        String content,
        TodayRecommendationFeedBadges badges,
        TodayRecommendationFeedTrack track,
        TodayRecommendationFeedUser user,
        TodayRecommendationFeedLike like
) {

    static TodayRecommendationFeedItemResponse from(PostFeedItemDto dto,
                                                    User author,
                                                    TodayRecommendationFeedBadges badges) {
        Post post = dto.post();
        Track track = post.getTrack();

        TodayRecommendationFeedTrack feedTrack = new TodayRecommendationFeedTrack(
                track.getTrackId(),
                track.getSongTitle(),
                track.getCoverImg(),
                track.getArtistName()
        );

        TodayRecommendationFeedUser feedUser = new TodayRecommendationFeedUser(
                author.getUserId(),
                author.getNickname(),
                author.getProfileImg()
        );

        TodayRecommendationFeedLike feedLike = new TodayRecommendationFeedLike(
                dto.isLiked(),
                post.getLikeCount()
        );

        return new TodayRecommendationFeedItemResponse(
                post.getPostId(),
                dto.isScrapped(),
                post.getContent(),
                badges,
                feedTrack,
                feedUser,
                feedLike
        );
    }
}

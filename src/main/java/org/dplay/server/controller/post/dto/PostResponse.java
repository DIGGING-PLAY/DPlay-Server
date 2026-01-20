package org.dplay.server.controller.post.dto;

import org.dplay.server.controller.track.dto.TrackDetailResponse;
import org.dplay.server.controller.user.dto.UserResponse;
import org.dplay.server.domain.post.dto.PostResultDto;

import java.time.LocalDate;

public record PostResponse(
        long postId,
        boolean isHost,
        boolean isScrapped,
        String content,
        LocalDate displayDate,
        TrackDetailResponse track,
        UserResponse user,
        LikeResponse like
) {

    public static PostResponse of(PostResultDto result) {
        return new PostResponse(
                result.postId(),
                result.isHost(),
                result.isScrapped(),
                result.content(),
                result.displayDate(),
                TrackDetailResponse.from(result.track()),
                UserResponse.from(result.user()),
                LikeResponse.of(
                        result.like().isLiked(),
                        result.like().likeCount()
                )
        );
    }
}

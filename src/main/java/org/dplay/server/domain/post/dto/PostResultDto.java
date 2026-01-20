package org.dplay.server.domain.post.dto;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.track.dto.TrackDetailResultDto;
import org.dplay.server.domain.user.dto.UserDetailResultDto;

import java.time.LocalDate;

public record PostResultDto(
        long postId,
        String content,
        boolean isHost,
        boolean isScrapped,
        LocalDate displayDate,
        TrackDetailResultDto track,
        UserDetailResultDto user,
        PostLikeResultDto like
) {

    public static PostResultDto of(
            Post post,
            boolean isHost,
            boolean isScrapped,
            TrackDetailResultDto track,
            UserDetailResultDto user,
            PostLikeResultDto like
    ) {
        return new PostResultDto(
                post.getPostId(),
                post.getContent(),
                isHost,
                isScrapped,
                post.getQuestion().getDisplayDate(),
                track,
                user,
                like
        );
    }
}
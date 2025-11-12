package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostFeedResultDto;

import java.time.LocalDate;

public interface PostFeedService {

    PostFeedResultDto getPastRecommendationFeed(
            Long userId,
            Long questionId,
            String cursor,
            Integer limit
    );

    PostFeedResultDto getTodayRecommendationFeed(
            Long userId,
            LocalDate today
    );
}

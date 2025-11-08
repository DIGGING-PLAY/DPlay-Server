package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostFeedResultDto;

public interface PostFeedService {

    PostFeedResultDto getPastRecommendationFeed(
            Long userId,
            Long questionId,
            String cursor,
            Integer limit
    );
}

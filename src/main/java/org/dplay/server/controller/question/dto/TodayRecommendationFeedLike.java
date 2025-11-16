package org.dplay.server.controller.question.dto;

public record TodayRecommendationFeedLike(
        boolean isLiked,
        int count
) {
}

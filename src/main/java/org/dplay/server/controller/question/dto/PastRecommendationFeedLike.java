package org.dplay.server.controller.question.dto;

public record PastRecommendationFeedLike(
        boolean isLiked,
        int count
) {
}

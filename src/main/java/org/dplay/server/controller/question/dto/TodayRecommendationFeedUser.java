package org.dplay.server.controller.question.dto;

public record TodayRecommendationFeedUser(
        Long userId,
        String nickname,
        String profileImg
) {
}

package org.dplay.server.controller.question.dto;

public record PastRecommendationFeedUser(
        Long userId,
        String nickname,
        String profileImg
) {
}

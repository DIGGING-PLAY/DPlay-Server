package org.dplay.server.controller.question.dto;

public record TodayRecommendationFeedBadges(
        boolean isEditorPick,
        boolean isPopular,
        boolean isNew
) {
}

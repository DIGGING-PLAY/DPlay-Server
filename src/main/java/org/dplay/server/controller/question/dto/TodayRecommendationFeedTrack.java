package org.dplay.server.controller.question.dto;

public record TodayRecommendationFeedTrack(
        String trackId,
        String songTitle,
        String coverImg,
        String artistName
) {
}

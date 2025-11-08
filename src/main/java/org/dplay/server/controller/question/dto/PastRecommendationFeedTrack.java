package org.dplay.server.controller.question.dto;

public record PastRecommendationFeedTrack(
        String trackId,
        String songTitle,
        String coverImg,
        String artistName
) {
}

package org.dplay.server.controller.question.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record PastRecommendationFeedRequest(
        String cursor,
        @Positive
        @Max(100)
        Integer limit
) {
}

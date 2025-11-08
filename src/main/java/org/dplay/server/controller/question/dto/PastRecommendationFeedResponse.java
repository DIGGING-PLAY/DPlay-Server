package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.global.util.DateTimeFormatUtil;

import java.util.List;

public record PastRecommendationFeedResponse(
        long questionId,
        String date,
        String title,
        boolean hasPosted,
        boolean locked,
        int visibleLimit,
        long totalCount,
        String nextCursor,
        List<PastRecommendationFeedItemResponse> items
) {

    public static PastRecommendationFeedResponse from(PostFeedResultDto dto) {
        List<PastRecommendationFeedItemResponse> itemResponses = dto.items().stream()
                .map(PastRecommendationFeedItemResponse::from)
                .toList();

        return new PastRecommendationFeedResponse(
                dto.questionId(),
                DateTimeFormatUtil.formatDate(dto.questionDate()),
                dto.title(),
                dto.hasPosted(),
                dto.locked(),
                dto.visibleLimit(),
                dto.totalCount(),
                dto.nextCursor(),
                itemResponses
        );
    }
}

package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.post.dto.PostFeedItemDto;
import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.global.util.DateTimeFormatUtil;

import java.util.ArrayList;
import java.util.List;

public record TodayRecommendationFeedResponse(
        long questionId,
        String date,
        String title,
        boolean hasPosted,
        boolean locked,
        long totalCount,
        List<TodayRecommendationFeedItemResponse> items
) {

    public static TodayRecommendationFeedResponse from(PostFeedResultDto dto) {
        List<PostFeedItemDto> feedItems = dto.items();

        List<TodayRecommendationFeedItemResponse> itemResponses = new ArrayList<>(feedItems.size());

        for (int index = 0; index < feedItems.size(); index++) {
            PostFeedItemDto item = feedItems.get(index);
            Post post = item.post();
            User author = post.getUser();

            Badge badge = createBadge(index);

            itemResponses.add(TodayRecommendationFeedItemResponse.from(item, author, badge));
        }

        return new TodayRecommendationFeedResponse(
                dto.questionId(),
                DateTimeFormatUtil.formatDate(dto.questionDate()),
                dto.title(),
                dto.hasPosted(),
                dto.locked(),
                dto.totalCount(),
                itemResponses
        );
    }

    /**
     * 뱃지 생성 로직 (인덱스 기반):
     * - 첫 번째 곡 (index 0): EDITOR
     * - 두 번째 곡 (index 1): BEST
     * - 세 번째 곡 (index 2): NEW
     * - 네 번째부터 (index 3+): null
     */
    private static Badge createBadge(int index) {
        return switch (index) {
            case 0 -> Badge.EDITOR;
            case 1 -> Badge.BEST;
            case 2 -> Badge.NEW;
            default -> null;
        };
    }
}

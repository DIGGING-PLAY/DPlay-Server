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

            TodayRecommendationFeedBadges badges = createBadges(index);

            itemResponses.add(TodayRecommendationFeedItemResponse.from(item, author, badges));
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
     * - 첫 번째 곡 (index 0): isEditorPick = true, 나머지 false
     * - 두 번째 곡 (index 1): isPopular = true, isNew = false, isEditorPick = false
     * - 세 번째 곡 (index 2): isNew = true, isPopular = false, isEditorPick = false
     * - 네 번째부터 (index 3+): 모든 뱃지 false
     */
    private static TodayRecommendationFeedBadges createBadges(int index) {
        return switch (index) {
            case 0 -> new TodayRecommendationFeedBadges(true, false, false);
            case 1 -> new TodayRecommendationFeedBadges(false, true, false);
            case 2 -> new TodayRecommendationFeedBadges(false, false, true);
            default -> new TodayRecommendationFeedBadges(false, false, false);
        };
    }
}

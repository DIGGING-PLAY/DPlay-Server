package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.post.dto.PostFeedItemDto;
import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.global.util.DateTimeFormatUtil;

import java.util.ArrayList;
import java.util.Comparator;
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
        boolean onlyEditorPicks = feedItems.stream().allMatch(PostFeedItemDto::isEditorPick);

        PostFeedItemDto popularItem = findMostPopularItem(feedItems);
        PostFeedItemDto newestItem = findNewestItem(feedItems);

        List<TodayRecommendationFeedItemResponse> itemResponses = new ArrayList<>(feedItems.size());

        for (int index = 0; index < feedItems.size(); index++) {
            PostFeedItemDto item = feedItems.get(index);
            Post post = item.post();
            User author = post.getUser();

            TodayRecommendationFeedBadges badges = createBadges(
                    item,
                    popularItem,
                    newestItem,
                    onlyEditorPicks,
                    index
            );

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

    private static TodayRecommendationFeedBadges createBadges(
            PostFeedItemDto item,
            PostFeedItemDto popularItem,
            PostFeedItemDto newestItem,
            boolean onlyEditorPicks,
            int index
    ) {
        if (onlyEditorPicks) {
            return switch (index) {
                case 0 -> new TodayRecommendationFeedBadges(true, false, false);
                case 1 -> new TodayRecommendationFeedBadges(true, true, false);
                case 2 -> new TodayRecommendationFeedBadges(true, false, true);
                default -> new TodayRecommendationFeedBadges(true, false, false);
            };
        }

        boolean isEditorPick = item.isEditorPick();
        boolean isPopular = popularItem != null && samePost(popularItem, item);
        boolean isNew = newestItem != null && samePost(newestItem, item);

        return new TodayRecommendationFeedBadges(isEditorPick, isPopular, isNew);
    }

    private static boolean samePost(PostFeedItemDto left, PostFeedItemDto right) {
        return left.post().getPostId().equals(right.post().getPostId());
    }

    private static PostFeedItemDto findMostPopularItem(List<PostFeedItemDto> items) {
        return items.stream()
                .max(Comparator
                        .comparingInt((PostFeedItemDto dto) -> dto.post().getLikeCount())
                        .thenComparingLong(dto -> -dto.post().getPostId()))
                .orElse(null);
    }

    private static PostFeedItemDto findNewestItem(List<PostFeedItemDto> items) {
        return items.stream()
                .max(Comparator
                        .comparing((PostFeedItemDto dto) -> dto.post().getCreatedAt(), Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparingLong(dto -> dto.post().getPostId()))
                .orElse(null);
    }
}

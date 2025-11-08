package org.dplay.server.domain.post.dto;

import java.time.LocalDate;
import java.util.List;

public record PostFeedResultDto(
        long questionId,
        LocalDate questionDate,
        String title,
        boolean hasPosted,
        boolean locked,
        int visibleLimit,
        long totalCount,
        String nextCursor,
        List<PostFeedItemDto> items
) {
}

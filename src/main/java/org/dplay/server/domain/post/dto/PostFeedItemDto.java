package org.dplay.server.domain.post.dto;

import org.dplay.server.domain.post.entity.Post;

public record PostFeedItemDto(
        Post post,
        boolean isEditorPick,
        boolean isPopular,
        boolean isNew,
        boolean isLiked,
        boolean isScrapped
) {
}

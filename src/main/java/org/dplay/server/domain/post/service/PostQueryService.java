package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.entity.Post;

import java.util.List;

public interface PostQueryService {

    boolean existsByQuestionAndUser(Long questionId, Long userId);

    long countByQuestion(Long questionId);

    List<Post> findFeedPosts(
            Long questionId,
            Long cursorLikeCount,
            Long cursorPostId,
            int limit,
            List<Long> excludePostIds
    );
}

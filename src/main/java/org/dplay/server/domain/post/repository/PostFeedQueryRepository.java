package org.dplay.server.domain.post.repository;

import org.dplay.server.domain.post.entity.Post;

import java.util.List;

public interface PostFeedQueryRepository {

    List<Post> findFeedPosts(
            Long questionId,
            Long cursorLikeCount,
            Long cursorPostId,
            int limit,
            List<Long> excludePostIds
    );
}

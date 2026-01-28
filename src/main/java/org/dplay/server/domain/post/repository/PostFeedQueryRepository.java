package org.dplay.server.domain.post.repository;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostSave;

import java.util.List;

public interface PostFeedQueryRepository {

    List<Post> findFeedPosts(
            Long questionId,
            Long cursorLikeCount,
            Long cursorPostId,
            int limit,
            List<Long> excludePostIds
    );

    List<Post> findLatestPosts(
            Long questionId,
            int limit,
            List<Long> excludePostIds
    );

    List<Post> findAllFeedPosts(
            Long questionId,
            List<Long> excludePostIds
    );

    List<Post> findPostsByUserDesc(
            Long userId,
            Long cursorPostId,
            int limit
    );

    List<PostSave> findSavedPostsByUserDesc(
            Long userId,
            Long cursorSaveId,
            int limit
    );
}

package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.user.entity.User;

import java.util.List;

public interface PostSaveService {
    /**
     * 스크랩을 추가합니다.
     *
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     */
    void addScrap(final long userId, final long postId);

    /**
     * 스크랩을 해제합니다.
     *
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     */
    void removeScrap(final long userId, final long postId);

    List<Long> findScrappedPostIds(User user, List<Post> posts);

    boolean isSaved(Post post, User user);
}


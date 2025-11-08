package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostLikeDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.user.entity.User;

import java.util.List;

public interface PostLikeService {
    /**
     * 좋아요를 추가합니다.
     *
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요 추가 후 게시글의 좋아요 개수
     */
    PostLikeDto addLike(final long userId, final long postId);

    /**
     * 좋아요를 해제합니다.
     *
     * @param userId 사용자 ID
     * @param postId 게시글 ID
     * @return 좋아요 해제 후 게시글의 좋아요 개수
     */
    PostLikeDto removeLike(final long userId, final long postId);

    List<Long> findLikedPostIds(User user, List<Post> posts);
}

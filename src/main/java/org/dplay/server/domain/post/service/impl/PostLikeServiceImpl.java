package org.dplay.server.domain.post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.post.dto.PostLikeDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostLike;
import org.dplay.server.domain.post.repository.PostLikeRepository;
import org.dplay.server.domain.post.service.PostLikeService;
import org.dplay.server.domain.post.service.PostService;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * PostLike 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostLikeServiceImpl implements PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostService postService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PostLikeDto addLike(final long userId, final long postId) {
        Post post = postService.findByPostId(postId);

        // User 조회 (TODO: 추후 UserService 구현 시 UserService.getUser(userId)로 변경)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));

        if (isLiked(post, user)) {
            throw new DPlayException(ResponseError.RESOURCE_ALREADY_EXISTS);
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();
        postLikeRepository.save(postLike);

        postService.incrementLikeCount(post);

        log.debug("좋아요 추가 성공 (postId: {}, userId: {}, likeCount: {})", postId, userId, post.getLikeCount());
        return PostLikeDto.of(post);
    }

    @Override
    @Transactional
    public PostLikeDto removeLike(final long userId, final long postId) {
        Post post = postService.findByPostId(postId);

        // User 조회 (TODO: 추후 UserService 구현 시 UserService.getUser(userId)로 변경)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));

        if (!isLiked(post, user)) {
            throw new DPlayException(ResponseError.TARGET_NOT_FOUND);
        }

        PostLike postLike = postLikeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new DPlayException(ResponseError.TARGET_NOT_FOUND));
        postLikeRepository.delete(postLike);

        postService.decrementLikeCount(post);

        log.debug("좋아요 해제 성공 (postId: {}, userId: {}, likeCount: {})", postId, userId, post.getLikeCount());
        return PostLikeDto.of(post);
    }

    @Override
    @Transactional
    public void deletePostSave(final User user) {
        postLikeRepository.deleteAllByUser(user);
    }

    @Override
    public List<Long> findLikedPostIds(User user, List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return List.of();
        }
        return postLikeRepository.findPostIdsByUserAndPosts(user, posts);
    }

    /**
     * 특정 유저가 해당 게시글에 좋아요를 눌렀는지 확인합니다.
     *
     * @param post Post 엔티티
     * @param user User 엔티티
     * @return 좋아요를 눌렀으면 true, 아니면 false
     */
    @Override
    public boolean isLiked(Post post, User user) {
        return postLikeRepository.existsByPostAndUser(post, user);
    }
}

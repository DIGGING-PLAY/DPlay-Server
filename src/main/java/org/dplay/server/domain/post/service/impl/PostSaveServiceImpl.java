package org.dplay.server.domain.post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostSave;
import org.dplay.server.domain.post.repository.PostSaveRepository;
import org.dplay.server.domain.post.service.PostSaveService;
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
 * PostSave 관련 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostSaveServiceImpl implements PostSaveService {

    private final PostSaveRepository postSaveRepository;
    private final PostService postService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void addScrap(final long userId, final long postId) {
        Post post = postService.findByPostId(postId);

        // User 조회 (TODO: 추후 UserService 구현 시 UserService.getUser(userId)로 변경)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));

        if (isSaved(post, user)) {
            throw new DPlayException(ResponseError.RESOURCE_ALREADY_EXISTS);
        }

        PostSave postSave = PostSave.builder()
                .post(post)
                .user(user)
                .build();
        postSaveRepository.save(postSave);

        postService.incrementSaveCount(post);

        log.debug("스크랩 추가 성공 (postId: {}, userId: {}, saveCount: {})", postId, userId, post.getSaveCount());
    }

    @Override
    @Transactional
    public void removeScrap(final long userId, final long postId) {
        Post post = postService.findByPostId(postId);

        // User 조회 (TODO: 추후 UserService 구현 시 UserService.getUser(userId)로 변경)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));

        if (!isSaved(post, user)) {
            throw new DPlayException(ResponseError.TARGET_NOT_FOUND);
        }

        PostSave postSave = postSaveRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new DPlayException(ResponseError.TARGET_NOT_FOUND));
        postSaveRepository.delete(postSave);

        postService.decrementSaveCount(post);

        log.debug("스크랩 해제 성공 (postId: {}, userId: {}, saveCount: {})", postId, userId, post.getSaveCount());
    }

    @Override
    public List<Long> findScrappedPostIds(User user, List<Post> posts) {
        if (CollectionUtils.isEmpty(posts)) {
            return List.of();
        }
        return postSaveRepository.findPostIdsByUserAndPosts(user, posts);
    }

    /**
     * 특정 유저가 해당 게시글을 스크랩했는지 확인합니다.
     *
     * @param post Post 엔티티
     * @param user User 엔티티
     * @return 스크랩했으면 true, 아니면 false
     */
    private boolean isSaved(Post post, User user) {
        return postSaveRepository.existsByPostAndUser(post, user);
    }
}


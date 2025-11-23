package org.dplay.server.domain.post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.post.dto.UserPostsResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostSave;
import org.dplay.server.domain.post.repository.PostRepository;
import org.dplay.server.domain.post.repository.PostSaveRepository;
import org.dplay.server.domain.post.service.PostSaveService;
import org.dplay.server.domain.post.service.PostService;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.service.UserService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
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
    private final PostRepository postRepository;
    private final UserService userService;
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 100;

    @Override
    @Transactional
    public void addScrap(final long userId, final long postId) {
        Post post = postService.findByPostId(postId);

        User user = userService.getUserById(userId);

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

        User user = userService.getUserById(userId);

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
    @Override
    public boolean isSaved(Post post, User user) {
        return postSaveRepository.existsByPostAndUser(post, user);
    }

    @Override
    public UserPostsResultDto getUserSaves(Long userId, String cursor, Integer limit) {
        userService.getUserById(userId);

        int visibleLimit = determineLimit(limit);

        Long cursorPostId = decodeCursor(cursor);

        long totalCount = postSaveRepository.countByUserUserId(userId);

        int fetchSize = visibleLimit + 1;
        List<Post> fetched = postRepository.findSavedPostsByUserDesc(userId, cursorPostId, fetchSize);

        String nextCursor = null;
        List<Post> resultPosts;
        if (fetched.size() > visibleLimit) {
            Post lastReturnedPost = fetched.get(visibleLimit - 1);
            nextCursor = encodeCursor(lastReturnedPost.getPostId());
            resultPosts = new ArrayList<>(fetched.subList(0, visibleLimit));
        } else {
            resultPosts = fetched;
        }

        return UserPostsResultDto.from(
                visibleLimit,
                totalCount,
                nextCursor,
                resultPosts
        );
    }

    private int determineLimit(Integer requestedLimit) {
        if (requestedLimit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(requestedLimit, MAX_LIMIT));
    }

    private Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            return Long.parseLong(decoded);
        } catch (Exception e) {
            log.warn("Invalid cursor received: {}", cursor, e);
            throw new DPlayException(ResponseError.INVALID_REQUEST_PARAMETER);
        }
    }

    private String encodeCursor(long postId) {
        String rawCursor = String.valueOf(postId);
        return Base64.getEncoder().encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }
}


package org.dplay.server.domain.post.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.post.dto.PostFeedItemDto;
import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.service.PostFeedService;
import org.dplay.server.domain.post.service.PostLikeService;
import org.dplay.server.domain.post.service.PostQueryService;
import org.dplay.server.domain.post.service.PostSaveService;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.question.entity.QuestionEditorPick;
import org.dplay.server.domain.question.service.QuestionEditorPickService;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PostFeedServiceImpl implements PostFeedService {

    private static final int LOCKED_VISIBLE_LIMIT = 3;
    private static final int DEFAULT_VISIBLE_LIMIT = 20;
    private static final int MAX_VISIBLE_LIMIT = 50;
    private static final int POPULAR_LIKE_THRESHOLD = 10;

    private final QuestionService questionService;
    private final QuestionEditorPickService questionEditorPickService;
    private final PostQueryService postQueryService;
    private final PostLikeService postLikeService;
    private final PostSaveService postSaveService;
    private final UserRepository userRepository;

    @Override
    public PostFeedResultDto getPastRecommendationFeed(
            Long userId,
            Long questionId,
            String cursor,
            Integer limit
    ) {
        // TODO : userService 로 바꾸기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));

        Question question = questionService.getQuestionById(questionId);

        List<QuestionEditorPick> editorPicks = questionEditorPickService.getOrderedEditorPicks(questionId);
        List<Post> editorPickPosts = editorPicks.stream()
                .map(QuestionEditorPick::getPost)
                .limit(LOCKED_VISIBLE_LIMIT)
                .toList();
        Set<Long> editorPickPostIds = editorPickPosts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toSet());

        boolean hasPosted = postQueryService.existsByQuestionAndUser(questionId, userId);
        boolean locked = !hasPosted;

        int visibleLimit = determineVisibleLimit(limit, locked);
        long totalCount = postQueryService.countByQuestion(questionId);

        Cursor decodedCursor = locked ? Cursor.EMPTY : decodeCursor(cursor);
        boolean isFirstPage = !locked && decodedCursor.isEmpty();

        List<Post> responsePosts = new ArrayList<>();
        String nextCursor = null;

        if (locked) {
            responsePosts.addAll(editorPickPosts);
        } else {
            int editorPickCount = editorPickPosts.size();
            int pageSize = isFirstPage ? Math.max(visibleLimit - editorPickCount, 0) : visibleLimit;

            List<Post> feedPosts = new ArrayList<>();
            if (pageSize > 0) {
                int fetchSize = pageSize + 1;
                List<Long> excludePostIds = new ArrayList<>(editorPickPostIds);
                List<Post> fetched = postQueryService.findFeedPosts(
                        questionId,
                        decodedCursor.likeCount(),
                        decodedCursor.postId(),
                        fetchSize,
                        excludePostIds
                );

                if (fetched.size() > pageSize) {
                    Post last = fetched.remove(fetched.size() - 1);
                    nextCursor = encodeCursor(last.getLikeCount(), last.getPostId());
                }

                feedPosts.addAll(fetched);
            }

            if (isFirstPage) {
                responsePosts.addAll(editorPickPosts);
            }
            responsePosts.addAll(feedPosts);
        }

        Set<Long> likedPostIds = fetchRelationPostIds(postLikeService::findLikedPostIds, responsePosts, user);
        Set<Long> savedPostIds = fetchRelationPostIds(postSaveService::findScrappedPostIds, responsePosts, user);

        List<PostFeedItemDto> items = buildFeedItems(
                responsePosts,
                editorPickPostIds,
                likedPostIds,
                savedPostIds,
                question
        );

        return new PostFeedResultDto(
                question.getQuestionId(),
                question.getDisplayDate(),
                question.getTitle(),
                hasPosted,
                locked,
                visibleLimit,
                totalCount,
                nextCursor,
                items
        );
    }

    private int determineVisibleLimit(Integer requestedLimit, boolean locked) {
        if (locked) {
            return LOCKED_VISIBLE_LIMIT;
        }

        int limit = requestedLimit != null ? requestedLimit : DEFAULT_VISIBLE_LIMIT;
        return Math.max(1, Math.min(limit, MAX_VISIBLE_LIMIT));
    }

    private Cursor decodeCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return Cursor.EMPTY;
        }

        try {
            String decoded = new String(Base64.getDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid cursor format");
            }
            Long likeCount = Long.parseLong(parts[0]);
            Long postId = Long.parseLong(parts[1]);
            return new Cursor(likeCount, postId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cursor received: {}", cursor, e);
            throw new DPlayException(ResponseError.INVALID_REQUEST_PARAMETER);
        }
    }

    private String encodeCursor(long likeCount, long postId) {
        String rawCursor = likeCount + ":" + postId;
        return Base64.getEncoder().encodeToString(rawCursor.getBytes(StandardCharsets.UTF_8));
    }

    private Set<Long> fetchRelationPostIds(RelationFetcher fetcher,
                                           List<Post> posts,
                                           User user) {
        if (CollectionUtils.isEmpty(posts)) {
            return new HashSet<>();
        }
        List<Long> postIds = fetcher.fetch(user, posts);
        if (postIds == null) {
            return new HashSet<>();
        }
        return new HashSet<>(postIds);
    }

    private List<PostFeedItemDto> buildFeedItems(List<Post> posts,
                                                 Set<Long> editorPickPostIds,
                                                 Set<Long> likedPostIds,
                                                 Set<Long> savedPostIds,
                                                 Question question) {
        return posts.stream()
                .map(post -> {
                    boolean isEditorPick = editorPickPostIds.contains(post.getPostId());
                    boolean isPopular = !isEditorPick && post.getLikeCount() >= POPULAR_LIKE_THRESHOLD;
                    boolean isNew = isNewPost(post, question);
                    boolean isLiked = likedPostIds.contains(post.getPostId());
                    boolean isScrapped = savedPostIds.contains(post.getPostId());
                    return new PostFeedItemDto(
                            post,
                            isEditorPick,
                            isPopular,
                            isNew,
                            isLiked,
                            isScrapped
                    );
                })
                .collect(Collectors.toList());
    }

    private boolean isNewPost(Post post, Question question) {
        LocalDateTime createdAt = post.getCreatedAt();
        return createdAt != null && createdAt.toLocalDate().isEqual(question.getDisplayDate());
    }

    private record Cursor(Long likeCount, Long postId) {
        private static final Cursor EMPTY = new Cursor(null, null);

        private boolean isEmpty() {
            return likeCount == null && postId == null;
        }
    }

    @FunctionalInterface
    private interface RelationFetcher {
        List<Long> fetch(User user, List<Post> posts);
    }
}

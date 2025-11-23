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
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
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

    private final Clock clock;
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
                    Post lastReturnedPost = fetched.get(pageSize - 1);
                    nextCursor = encodeCursor(lastReturnedPost.getLikeCount(), lastReturnedPost.getPostId());
                    feedPosts.addAll(fetched.subList(0, pageSize));
                } else {
                    feedPosts.addAll(fetched);
                }
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
                Collections.emptySet(),
                Collections.emptySet(),
                likedPostIds,
                savedPostIds,
                question
        );

        int effectiveVisibleLimit = locked ? LOCKED_VISIBLE_LIMIT : visibleLimit;

        return new PostFeedResultDto(
                question.getQuestionId(),
                question.getDisplayDate(),
                question.getTitle(),
                hasPosted,
                locked,
                effectiveVisibleLimit,
                totalCount,
                nextCursor,
                items
        );
    }

    @Override
    public PostFeedResultDto getTodayRecommendationFeed(Long userId) {
        LocalDate today = getTodayDate();
        Question question = questionService.getQuestionByDate(today);
        Long questionId = question.getQuestionId();

        // TODO : UserService 로 고치기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));

        List<QuestionEditorPick> editorPicks = questionEditorPickService.getOrderedEditorPicks(questionId);
        long totalCount = postQueryService.countByQuestion(questionId);

        boolean hasPosted = postQueryService.existsByQuestionAndUser(questionId, userId);
        if (hasPosted) {
            return buildUnlockedTodayFeed(user, question, editorPicks, totalCount);
        }

        return buildLockedTodayFeed(user, question, editorPicks, totalCount);
    }

    private PostFeedResultDto buildLockedTodayFeed(User user,
                                                   Question question,
                                                   List<QuestionEditorPick> editorPicks,
                                                   long totalCount) {
        List<Post> editorPickPosts = editorPicks.stream()
                .map(QuestionEditorPick::getPost)
                .filter(post -> post != null && post.getPostId() != null)
                .limit(LOCKED_VISIBLE_LIMIT)
                .toList();

        Set<Long> editorPickPostIds = editorPickPosts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<Long> likedPostIds = fetchRelationPostIds(postLikeService::findLikedPostIds, editorPickPosts, user);
        Set<Long> savedPostIds = fetchRelationPostIds(postSaveService::findScrappedPostIds, editorPickPosts, user);

        List<PostFeedItemDto> items = buildFeedItems(
                editorPickPosts,
                editorPickPostIds,
                Collections.emptySet(),
                Collections.emptySet(),
                likedPostIds,
                savedPostIds,
                question
        );

        return new PostFeedResultDto(
                question.getQuestionId(),
                question.getDisplayDate(),
                question.getTitle(),
                false,
                true,
                LOCKED_VISIBLE_LIMIT,
                totalCount,
                null,
                items
        );
    }

    private PostFeedResultDto buildUnlockedTodayFeed(User user,
                                                     Question question,
                                                     List<QuestionEditorPick> editorPicks,
                                                     long totalCount) {
        Long questionId = question.getQuestionId();

        List<Post> editorPickPosts = editorPicks.stream()
                .map(QuestionEditorPick::getPost)
                .filter(post -> post != null && post.getPostId() != null)
                .toList();

        Set<Long> editorPickPostIds = editorPickPosts.stream()
                .map(Post::getPostId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<Post> nonEditorPosts = postQueryService.findAllFeedPosts(
                questionId,
                new ArrayList<>(editorPickPostIds)
        );

        List<Post> resultPosts = new ArrayList<>();
        Set<Long> forcedPopularPostIds = new HashSet<>();
        Set<Long> forcedNewPostIds = new HashSet<>();

        Set<Long> usedEditorPickIds = new LinkedHashSet<>();
        Post primaryEditorPick = selectNextEditorPick(editorPickPosts, usedEditorPickIds);
        if (primaryEditorPick != null) {
            resultPosts.add(primaryEditorPick);
        }

        List<Post> candidatePosts = new ArrayList<>();
        editorPickPosts.stream()
                .filter(post -> post.getPostId() != null && !usedEditorPickIds.contains(post.getPostId()))
                .forEach(candidatePosts::add);
        candidatePosts.addAll(nonEditorPosts);

        Post popularPost = candidatePosts.stream()
                .max(Comparator
                        .comparingInt(Post::getLikeCount)
                        .thenComparingLong(Post::getPostId))
                .orElse(null);

        if (popularPost != null) {
            forcedPopularPostIds.add(popularPost.getPostId());
            addIfAbsent(resultPosts, popularPost);
        }

        Post newestPost = candidatePosts.stream()
                .max(Comparator
                        .comparing(Post::getCreatedAt, Comparator.nullsFirst(LocalDateTime::compareTo))
                        .thenComparingLong(Post::getPostId))
                .orElse(null);

        if (newestPost != null) {
            forcedNewPostIds.add(newestPost.getPostId());
            addIfAbsent(resultPosts, newestPost);
        }

        List<Post> remainingPosts = candidatePosts.stream()
                .filter(post -> resultPosts.stream()
                        .map(Post::getPostId)
                        .noneMatch(id -> Objects.equals(id, post.getPostId())))
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(remainingPosts, ThreadLocalRandom.current());
        resultPosts.addAll(remainingPosts);

        Set<Long> likedPostIds = fetchRelationPostIds(postLikeService::findLikedPostIds, resultPosts, user);
        Set<Long> savedPostIds = fetchRelationPostIds(postSaveService::findScrappedPostIds, resultPosts, user);

        List<PostFeedItemDto> items = buildFeedItems(
                resultPosts,
                editorPickPostIds,
                forcedPopularPostIds,
                forcedNewPostIds,
                likedPostIds,
                savedPostIds,
                question
        );

        return new PostFeedResultDto(
                questionId,
                question.getDisplayDate(),
                question.getTitle(),
                true,
                false,
                resultPosts.size(),
                totalCount,
                null,
                items
        );
    }

    private void addIfAbsent(List<Post> posts, Post candidate) {
        boolean exists = posts.stream()
                .anyMatch(post -> Objects.equals(post.getPostId(), candidate.getPostId()));
        if (!exists) {
            posts.add(candidate);
        }
    }

    private void fillWithRemainingEditorPicks(List<Post> editorPickPosts,
                                              Set<Long> usedEditorPickIds,
                                              List<Post> selectedPosts) {
        while (selectedPosts.size() < LOCKED_VISIBLE_LIMIT) {
            Post nextEditorPick = selectNextEditorPick(editorPickPosts, usedEditorPickIds);
            if (nextEditorPick == null) {
                break;
            }
            selectedPosts.add(nextEditorPick);
        }
    }

    private Post selectNextEditorPick(List<Post> editorPickPosts,
                                      Set<Long> usedEditorPickIds) {
        for (Post editorPick : editorPickPosts) {
            Long postId = editorPick.getPostId();
            if (postId == null) {
                continue;
            }
            if (usedEditorPickIds.add(postId)) {
                return editorPick;
            }
        }
        return null;
    }

    private List<Long> collectPostIds(List<Post> posts) {
        return posts.stream()
                .map(Post::getPostId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Post selectPopularPost(Long questionId, List<Long> excludedPostIds) {
        List<Post> candidates = postQueryService.findFeedPosts(
                questionId,
                null,
                null,
                1,
                excludedPostIds
        );

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(0);
    }

    private Post selectNewestPost(Long questionId, List<Long> excludedPostIds) {
        List<Post> candidates = postQueryService.findLatestPosts(
                questionId,
                1,
                excludedPostIds
        );

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(0);
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
                                                 Set<Long> forcedPopularPostIds,
                                                 Set<Long> forcedNewPostIds,
                                                 Set<Long> likedPostIds,
                                                 Set<Long> savedPostIds,
                                                 Question question) {
        return posts.stream()
                .map(post -> {
                    boolean isEditorPick = editorPickPostIds.contains(post.getPostId());
                    boolean isPopular = forcedPopularPostIds.contains(post.getPostId())
                            || (!isEditorPick && post.getLikeCount() >= POPULAR_LIKE_THRESHOLD);
                    boolean isNew = forcedNewPostIds.contains(post.getPostId())
                            || isNewPost(post, question);
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

    private LocalDate getTodayDate() {
        return LocalDate.now(clock);
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

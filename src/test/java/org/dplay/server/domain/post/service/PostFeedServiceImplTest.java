package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostFeedItemDto;
import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.service.impl.PostFeedServiceImpl;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.question.entity.QuestionEditorPick;
import org.dplay.server.domain.question.service.QuestionEditorPickService;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostFeedServiceImplTest {

    @Mock
    private QuestionService questionService;
    @Mock
    private QuestionEditorPickService questionEditorPickService;
    @Mock
    private PostQueryService postQueryService;
    @Mock
    private PostLikeService postLikeService;
    @Mock
    private PostSaveService postSaveService;
    @Mock
    private UserRepository userRepository;

    private PostFeedServiceImpl postFeedService;

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Long USER_ID = 100L;
    private static final Long QUESTION_ID = 200L;
    private static final LocalDate QUESTION_DATE = LocalDate.of(2025, 11, 5);

    private User user;
    private Question question;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(QUESTION_DATE.atStartOfDay(ZONE).toInstant(), ZONE);
        postFeedService = new PostFeedServiceImpl(
                fixedClock,
                questionService,
                questionEditorPickService,
                postQueryService,
                postLikeService,
                postSaveService,
                userRepository
        );

        user = User.builder()
                .platform(Platform.KAKAO)
                .platformId("platform-1")
                .nickname("테스트유저")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "userId", USER_ID);

        question = Question.builder()
                .title("배고플 때 듣는 노래는?")
                .displayDate(QUESTION_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", QUESTION_ID);
    }

    @Test
    @DisplayName("사용자가 게시글을 작성하지 않았다면 에디터픽만 반환하고 잠금 상태로 응답한다")
    void getPastRecommendationFeed_lockedUser_returnsEditorPicksOnly() {
        // Given
        Post editorPost = createPost(1L, 5, "editor pick");
        QuestionEditorPick editorPick = QuestionEditorPick.builder()
                .question(question)
                .post(editorPost)
                .position(1)
                .build();

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(questionService.getQuestionById(QUESTION_ID)).thenReturn(question);
        when(questionEditorPickService.getOrderedEditorPicks(QUESTION_ID))
                .thenReturn(List.of(editorPick));
        when(postQueryService.existsByQuestionAndUser(QUESTION_ID, USER_ID)).thenReturn(false);
        when(postQueryService.countByQuestion(QUESTION_ID)).thenReturn(1L);
        when(postLikeService.findLikedPostIds(eq(user), anyList())).thenReturn(List.of());
        when(postSaveService.findScrappedPostIds(eq(user), anyList())).thenReturn(List.of());

        // When
        PostFeedResultDto result = postFeedService.getPastRecommendationFeed(USER_ID, QUESTION_ID, null, null);

        // Then
        assertThat(result.locked()).isTrue();
        assertThat(result.hasPosted()).isFalse();
        assertThat(result.visibleLimit()).isEqualTo(3);
        assertThat(result.nextCursor()).isNull();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).isEditorPick()).isTrue();
        verify(postQueryService, never()).findFeedPosts(anyLong(), any(), any(), anyInt(), anyList());
    }

    @Test
    @DisplayName("사용자가 게시글을 작성한 경우 에디터픽과 다른 게시글을 모두 반환한다")
    void getPastRecommendationFeed_unlockedUser_returnsCombinedPosts() {
        // Given
        Post editorPost1 = createPost(1L, 100, "editor pick 1");
        Post editorPost2 = createPost(2L, 90, "editor pick 2");
        Post editorPost3 = createPost(3L, 80, "editor pick 3");
        QuestionEditorPick editorPick1 = QuestionEditorPick.builder()
                .question(question)
                .post(editorPost1)
                .position(1)
                .build();
        QuestionEditorPick editorPick2 = QuestionEditorPick.builder()
                .question(question)
                .post(editorPost2)
                .position(2)
                .build();
        QuestionEditorPick editorPick3 = QuestionEditorPick.builder()
                .question(question)
                .post(editorPost3)
                .position(3)
                .build();

        Post feedPost1 = createPost(10L, 50, "feed 1");
        Post feedPost2 = createPost(11L, 40, "feed 2");
        Post feedPost3 = createPost(12L, 30, "excess feed");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(questionService.getQuestionById(QUESTION_ID)).thenReturn(question);
        when(questionEditorPickService.getOrderedEditorPicks(QUESTION_ID))
                .thenReturn(List.of(editorPick1, editorPick2, editorPick3));
        when(postQueryService.existsByQuestionAndUser(QUESTION_ID, USER_ID)).thenReturn(true);
        when(postQueryService.countByQuestion(QUESTION_ID)).thenReturn(5L);
        when(postQueryService.findFeedPosts(eq(QUESTION_ID), isNull(), isNull(), eq(3), anyList()))
                .thenReturn(new ArrayListBuilder<Post>()
                        .add(feedPost1)
                        .add(feedPost2)
                        .add(feedPost3)
                        .build());

        when(postLikeService.findLikedPostIds(eq(user), anyList()))
                .thenReturn(List.of(feedPost1.getPostId()));
        when(postSaveService.findScrappedPostIds(eq(user), anyList()))
                .thenReturn(List.of(feedPost2.getPostId()));

        // When
        PostFeedResultDto result = postFeedService.getPastRecommendationFeed(USER_ID, QUESTION_ID, null, 5);

        // Then
        assertThat(result.locked()).isFalse();
        assertThat(result.hasPosted()).isTrue();
        assertThat(result.visibleLimit()).isEqualTo(5);
        assertThat(result.nextCursor()).isNotNull();
        assertThat(result.items()).hasSize(5); // 3 editor picks + 2 feed posts
        assertThat(result.items().get(0).post().getPostId()).isEqualTo(editorPost1.getPostId());
        assertThat(result.items().get(3).post().getPostId()).isEqualTo(feedPost1.getPostId());
        assertThat(result.items().get(3).isLiked()).isTrue();
        assertThat(result.items().get(4).isScrapped()).isTrue();

        verify(postQueryService).findFeedPosts(eq(QUESTION_ID), isNull(), isNull(), eq(3), anyList());
    }

    @Test
    @DisplayName("오늘 추천글 조회에서 게시글을 작성했다면 에디터픽1-인기-최신-나머지 랜덤 순으로 반환한다")
    void getTodayRecommendationFeed_hasPosted_returnsEditorPopularNewestAndRandom() {
        // Given
        Post editorPick1 = createPost(1L, 10, "editor pick 1", QUESTION_DATE.atStartOfDay().plusHours(8));
        Post editorPick2 = createPost(2L, 40, "editor pick 2", QUESTION_DATE.atStartOfDay().plusHours(9));
        Post editorPick3 = createPost(3L, 30, "editor pick 3", QUESTION_DATE.atStartOfDay().plusHours(10));

        QuestionEditorPick pick1 = QuestionEditorPick.builder().question(question).post(editorPick1).position(1).build();
        QuestionEditorPick pick2 = QuestionEditorPick.builder().question(question).post(editorPick2).position(2).build();
        QuestionEditorPick pick3 = QuestionEditorPick.builder().question(question).post(editorPick3).position(3).build();

        Post userPopular = createPost(10L, 80, "user popular", QUESTION_DATE.atStartOfDay().plusHours(12));
        Post userNewest = createPost(11L, 20, "user newest", QUESTION_DATE.atStartOfDay().plusHours(23));
        Post userAnother = createPost(12L, 15, "user another", QUESTION_DATE.atStartOfDay().plusHours(15));

        when(questionService.getQuestionByDate(QUESTION_DATE)).thenReturn(question);
        when(postQueryService.existsByQuestionAndUser(QUESTION_ID, USER_ID)).thenReturn(true);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(questionEditorPickService.getOrderedEditorPicks(QUESTION_ID))
                .thenReturn(List.of(pick1, pick2, pick3));
        when(postQueryService.countByQuestion(QUESTION_ID)).thenReturn(6L);
        when(postQueryService.findAllFeedPosts(eq(QUESTION_ID), anyList()))
                .thenReturn(List.of(userPopular, userNewest, userAnother));
        when(postLikeService.findLikedPostIds(eq(user), anyList())).thenReturn(List.of(userPopular.getPostId()));
        when(postSaveService.findScrappedPostIds(eq(user), anyList())).thenReturn(List.of(userNewest.getPostId()));

        // When
        PostFeedResultDto result = postFeedService.getTodayRecommendationFeed(USER_ID);

        // Then
        assertThat(result.hasPosted()).isTrue();
        assertThat(result.locked()).isFalse();
        List<Long> postIds = result.items().stream()
                .map(item -> item.post().getPostId())
                .toList();

        assertThat(postIds.get(0)).isEqualTo(editorPick1.getPostId());

        PostFeedItemDto popularItem = result.items().stream()
                .filter(item -> item.post().getPostId().equals(userPopular.getPostId()))
                .findFirst()
                .orElseThrow();
        assertThat(popularItem.isPopular()).isTrue();

        PostFeedItemDto newestItem = result.items().stream()
                .filter(item -> item.post().getPostId().equals(userNewest.getPostId()))
                .findFirst()
                .orElseThrow();
        assertThat(newestItem.isNew()).isTrue();

        Set<Long> expectedIds = Set.of(
                editorPick1.getPostId(),
                editorPick2.getPostId(),
                editorPick3.getPostId(),
                userPopular.getPostId(),
                userNewest.getPostId(),
                userAnother.getPostId()
        );
        assertThat(postIds).containsExactlyInAnyOrderElementsOf(expectedIds);

        verify(postQueryService).findAllFeedPosts(eq(QUESTION_ID), anyList());
        verify(postQueryService, never()).findFeedPosts(anyLong(), any(), any(), anyInt(), anyList());
        verify(postQueryService, never()).findLatestPosts(anyLong(), anyInt(), anyList());
    }

    @Test
    @DisplayName("오늘 추천글 조회에서 게시글을 작성하지 않았다면 에디터픽만 보여준다")
    void getTodayRecommendationFeed_lockedUser_returnsOnlyEditorPicks() {
        // Given
        Post editorPick1 = createPost(1L, 15, "editor pick 1", QUESTION_DATE.atStartOfDay().plusHours(8));
        Post editorPick2 = createPost(2L, 12, "editor pick 2", QUESTION_DATE.atStartOfDay().plusHours(9));
        Post editorPick3 = createPost(3L, 8, "editor pick 3", QUESTION_DATE.atStartOfDay().plusHours(10));

        QuestionEditorPick pick1 = QuestionEditorPick.builder()
                .question(question)
                .post(editorPick1)
                .position(1)
                .build();
        QuestionEditorPick pick2 = QuestionEditorPick.builder()
                .question(question)
                .post(editorPick2)
                .position(2)
                .build();
        QuestionEditorPick pick3 = QuestionEditorPick.builder()
                .question(question)
                .post(editorPick3)
                .position(3)
                .build();

        when(questionService.getQuestionByDate(QUESTION_DATE)).thenReturn(question);
        when(postQueryService.existsByQuestionAndUser(QUESTION_ID, USER_ID)).thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(questionEditorPickService.getOrderedEditorPicks(QUESTION_ID)).thenReturn(List.of(pick1, pick2, pick3));
        when(postQueryService.countByQuestion(QUESTION_ID)).thenReturn(10L);
        when(postLikeService.findLikedPostIds(eq(user), anyList())).thenReturn(List.of());
        when(postSaveService.findScrappedPostIds(eq(user), anyList())).thenReturn(List.of());

        // When
        PostFeedResultDto result = postFeedService.getTodayRecommendationFeed(USER_ID);

        // Then
        assertThat(result.hasPosted()).isFalse();
        assertThat(result.locked()).isTrue();
        assertThat(result.items()).hasSize(3);
        assertThat(result.items().stream().allMatch(PostFeedItemDto::isEditorPick)).isTrue();
        assertThat(result.items().stream().map(item -> item.post().getPostId()))
                .containsExactly(editorPick1.getPostId(), editorPick2.getPostId(), editorPick3.getPostId());

        verify(postQueryService, never()).findFeedPosts(anyLong(), any(), any(), anyInt(), anyList());
        verify(postQueryService, never()).findLatestPosts(anyLong(), anyInt(), anyList());
    }

    @Test
    @DisplayName("오늘 추천글 조회에서 에디터픽이 하나뿐이라면 해당 곡만 보여준다")
    void getTodayRecommendationFeed_singleEditorPick_returnsSingleItem() {
        // Given
        Post editorPick1 = createPost(1L, 10, "editor pick 1", QUESTION_DATE.atStartOfDay().plusHours(8));
        QuestionEditorPick pick1 = QuestionEditorPick.builder().question(question).post(editorPick1).position(1).build();

        when(questionService.getQuestionByDate(QUESTION_DATE)).thenReturn(question);
        when(postQueryService.existsByQuestionAndUser(QUESTION_ID, USER_ID)).thenReturn(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(questionEditorPickService.getOrderedEditorPicks(QUESTION_ID)).thenReturn(List.of(pick1));
        when(postQueryService.countByQuestion(QUESTION_ID)).thenReturn(0L);
        when(postLikeService.findLikedPostIds(eq(user), anyList())).thenReturn(List.of());
        when(postSaveService.findScrappedPostIds(eq(user), anyList())).thenReturn(List.of());

        // When
        PostFeedResultDto result = postFeedService.getTodayRecommendationFeed(USER_ID);

        // Then
        assertThat(result.hasPosted()).isFalse();
        assertThat(result.locked()).isTrue();
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).post().getPostId()).isEqualTo(editorPick1.getPostId());
        assertThat(result.items().get(0).isEditorPick()).isTrue();

        verify(postQueryService, never()).findFeedPosts(anyLong(), any(), any(), anyInt(), anyList());
        verify(postQueryService, never()).findLatestPosts(anyLong(), anyInt(), anyList());
    }

    private Post createPost(Long postId, int likeCount, String content) {
        return createPost(postId, likeCount, content, QUESTION_DATE.atStartOfDay().plusHours(1));
    }

    private Post createPost(Long postId, int likeCount, String content, LocalDateTime createdAt) {
        Track track = Track.builder()
                .trackId("apple:" + postId)
                .songTitle("Song " + postId)
                .artistName("Artist " + postId)
                .coverImg("https://example.com/" + postId + ".jpg")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content(content)
                .likeCount(likeCount)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);
        ReflectionTestUtils.setField(post, "createdAt", createdAt);

        return post;
    }

    private static class ArrayListBuilder<T> {
        private final List<T> delegate = new java.util.ArrayList<>();

        ArrayListBuilder<T> add(T value) {
            delegate.add(value);
            return this;
        }

        List<T> build() {
            return delegate;
        }
    }
}


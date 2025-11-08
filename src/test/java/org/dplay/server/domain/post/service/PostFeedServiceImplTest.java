package org.dplay.server.domain.post.service;

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
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private static final Long USER_ID = 100L;
    private static final Long QUESTION_ID = 200L;
    private static final LocalDate QUESTION_DATE = LocalDate.of(2025, 11, 5);

    private User user;
    private Question question;

    @BeforeEach
    void setUp() {
        postFeedService = new PostFeedServiceImpl(
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
    @DisplayName("사용자가 게시글을 작성한 경우 커서 기반 페이징으로 추천글을 조회한다")
    void getPastRecommendationFeed_unlockedUser_returnsPagedPosts() {
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
        when(postQueryService.findFeedPosts(
                eq(QUESTION_ID),
                isNull(),
                isNull(),
                eq(3),
                ArgumentMatchers.<List<Long>>any()
        )).thenReturn(new ArrayListBuilder<Post>()
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
    @DisplayName("다음 페이지 조회 시 에디터픽은 포함되지 않는다")
    void getPastRecommendationFeed_nextPage_excludesEditorPicks() {
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

        Post nextPost1 = createPost(20L, 25, "next page 1");
        Post nextPost2 = createPost(19L, 25, "next page 2");

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(questionService.getQuestionById(QUESTION_ID)).thenReturn(question);
        when(questionEditorPickService.getOrderedEditorPicks(QUESTION_ID))
                .thenReturn(List.of(editorPick1, editorPick2, editorPick3));
        when(postQueryService.existsByQuestionAndUser(QUESTION_ID, USER_ID)).thenReturn(true);
        when(postQueryService.countByQuestion(QUESTION_ID)).thenReturn(10L);

        String cursor = Base64.getEncoder().encodeToString("25:18".getBytes());
        when(postQueryService.findFeedPosts(
                eq(QUESTION_ID),
                eq(25L),
                eq(18L),
                eq(7),
                ArgumentMatchers.<List<Long>>any()
        )).thenReturn(List.of(nextPost1, nextPost2));

        when(postLikeService.findLikedPostIds(eq(user), anyList())).thenReturn(List.of());
        when(postSaveService.findScrappedPostIds(eq(user), anyList())).thenReturn(List.of());

        // When
        PostFeedResultDto result = postFeedService.getPastRecommendationFeed(USER_ID, QUESTION_ID, cursor, 6);

        // Then
        assertThat(result.locked()).isFalse();
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().stream().allMatch(item -> !item.isEditorPick())).isTrue();
        verify(postQueryService).findFeedPosts(eq(QUESTION_ID), eq(25L), eq(18L), eq(7), anyList());
    }

    @Test
    @DisplayName("잘못된 커서를 전달하면 INVALID_REQUEST_PARAMETER 예외를 던진다")
    void getPastRecommendationFeed_invalidCursor_throwsException() {
        // Given
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(questionService.getQuestionById(QUESTION_ID)).thenReturn(question);
        when(questionEditorPickService.getOrderedEditorPicks(QUESTION_ID))
                .thenReturn(List.of());
        when(postQueryService.existsByQuestionAndUser(QUESTION_ID, USER_ID)).thenReturn(true);
        when(postQueryService.countByQuestion(QUESTION_ID)).thenReturn(0L);

        // When & Then
        assertThatThrownBy(() -> postFeedService.getPastRecommendationFeed(USER_ID, QUESTION_ID, "invalid_cursor", 20))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.INVALID_REQUEST_PARAMETER);
    }

    private Post createPost(Long postId, int likeCount, String content) {
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
        ReflectionTestUtils.setField(post, "createdAt", QUESTION_DATE.atStartOfDay().plusHours(1));

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


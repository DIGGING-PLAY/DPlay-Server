package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostSave;
import org.dplay.server.domain.post.repository.PostSaveRepository;
import org.dplay.server.domain.post.service.impl.PostSaveServiceImpl;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostSaveServiceImplTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 11, 3);

    @Mock
    private PostSaveRepository postSaveRepository;
    @Mock
    private PostService postService;
    @Mock
    private UserRepository userRepository;

    private PostSaveServiceImpl postSaveService;

    @BeforeEach
    void setUp() {
        postSaveService = new PostSaveServiceImpl(
                postSaveRepository,
                postService,
                userRepository
        );
    }

    @Test
    @DisplayName("스크랩을 정상적으로 추가한다")
    void addScrap_ok() {
        // Given
        long userId = 1L;
        long postId = 1L;

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        Track track = Track.builder()
                .trackId("apple:1678382")
                .songTitle("Blueming")
                .artistName("IU")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("이 노래 짱!")
                .likeCount(0)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        PostSave postSave = PostSave.builder()
                .post(post)
                .user(user)
                .build();

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(postSaveRepository.existsByPostAndUser(post, user)).thenReturn(false);
        when(postSaveRepository.save(any(PostSave.class))).thenReturn(postSave);
        doNothing().when(postService).incrementSaveCount(post);

        // When
        postSaveService.addScrap(userId, postId);

        // Then
        verify(postService, times(1)).findByPostId(postId);
        verify(postSaveRepository, times(1)).save(any(PostSave.class));
        verify(postService, times(1)).incrementSaveCount(post);
    }

    @Test
    @DisplayName("존재하지 않는 postId로 스크랩을 추가하려고 하면 TARGET_NOT_FOUND 예외를 던진다")
    void addScrap_postNotFound_throws() {
        // Given
        long userId = 1L;
        long postId = 999L;

        when(postService.findByPostId(postId))
                .thenThrow(new DPlayException(ResponseError.TARGET_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postSaveService.addScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, never()).findById(anyLong());
        verify(postSaveRepository, never()).save(any(PostSave.class));
    }

    @Test
    @DisplayName("존재하지 않는 userId로 스크랩을 추가하려고 하면 USER_NOT_FOUND 예외를 던진다")
    void addScrap_userNotFound_throws() {
        // Given
        long userId = 999L;
        long postId = 1L;

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        Track track = Track.builder()
                .trackId("apple:1678382")
                .songTitle("Blueming")
                .artistName("IU")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("이 노래 짱!")
                .likeCount(0)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postSaveService.addScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, times(1)).findById(userId);
        verify(postSaveRepository, never()).save(any(PostSave.class));
    }

    @Test
    @DisplayName("이미 스크랩을 한 경우 RESOURCE_ALREADY_EXISTS 예외를 던진다")
    void addScrap_alreadyScrapped_throws() {
        // Given
        long userId = 1L;
        long postId = 1L;

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        Track track = Track.builder()
                .trackId("apple:1678382")
                .songTitle("Blueming")
                .artistName("IU")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("이 노래 짱!")
                .likeCount(0)
                .saveCount(1)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(postSaveRepository.existsByPostAndUser(post, user)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> postSaveService.addScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.RESOURCE_ALREADY_EXISTS);

        verify(postSaveRepository, never()).save(any(PostSave.class));
        verify(postService, never()).incrementSaveCount(any(Post.class));
    }

    @Test
    @DisplayName("스크랩을 정상적으로 해제한다")
    void removeScrap_ok() {
        // Given
        long userId = 1L;
        long postId = 1L;

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        Track track = Track.builder()
                .trackId("apple:1678382")
                .songTitle("Blueming")
                .artistName("IU")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("이 노래 짱!")
                .likeCount(0)
                .saveCount(1)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        PostSave postSave = PostSave.builder()
                .post(post)
                .user(user)
                .build();

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(postSaveRepository.existsByPostAndUser(post, user)).thenReturn(true);
        when(postSaveRepository.findByPostAndUser(post, user)).thenReturn(java.util.Optional.of(postSave));
        doNothing().when(postSaveRepository).delete(postSave);
        doNothing().when(postService).decrementSaveCount(post);

        // When
        postSaveService.removeScrap(userId, postId);

        // Then
        verify(postService, times(1)).findByPostId(postId);
        verify(postSaveRepository, times(1)).delete(postSave);
        verify(postService, times(1)).decrementSaveCount(post);
    }

    @Test
    @DisplayName("존재하지 않는 postId로 스크랩을 해제하려고 하면 TARGET_NOT_FOUND 예외를 던진다")
    void removeScrap_postNotFound_throws() {
        // Given
        long userId = 1L;
        long postId = 999L;

        when(postService.findByPostId(postId))
                .thenThrow(new DPlayException(ResponseError.TARGET_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postSaveService.removeScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, never()).findById(anyLong());
        verify(postSaveRepository, never()).delete(any(PostSave.class));
    }

    @Test
    @DisplayName("존재하지 않는 userId로 스크랩을 해제하려고 하면 USER_NOT_FOUND 예외를 던진다")
    void removeScrap_userNotFound_throws() {
        // Given
        long userId = 999L;
        long postId = 1L;

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        Track track = Track.builder()
                .trackId("apple:1678382")
                .songTitle("Blueming")
                .artistName("IU")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("이 노래 짱!")
                .likeCount(0)
                .saveCount(1)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postSaveService.removeScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, times(1)).findById(userId);
        verify(postSaveRepository, never()).delete(any(PostSave.class));
    }

    @Test
    @DisplayName("스크랩을 하지 않은 경우 TARGET_NOT_FOUND 예외를 던진다")
    void removeScrap_notScrapped_throws() {
        // Given
        long userId = 1L;
        long postId = 1L;

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        Track track = Track.builder()
                .trackId("apple:1678382")
                .songTitle("Blueming")
                .artistName("IU")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("이 노래 짱!")
                .likeCount(0)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(postSaveRepository.existsByPostAndUser(post, user)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> postSaveService.removeScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postSaveRepository, never()).delete(any(PostSave.class));
        verify(postService, never()).decrementSaveCount(any(Post.class));
    }
}


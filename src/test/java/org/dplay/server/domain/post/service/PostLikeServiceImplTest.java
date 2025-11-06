package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostLikeDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostLike;
import org.dplay.server.domain.post.repository.PostLikeRepository;
import org.dplay.server.domain.post.service.impl.PostLikeServiceImpl;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostLikeServiceImplTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 11, 3);

    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private PostService postService;
    @Mock
    private UserRepository userRepository;

    private PostLikeServiceImpl postLikeService;

    @BeforeEach
    void setUp() {
        postLikeService = new PostLikeServiceImpl(
                postLikeRepository,
                postService,
                userRepository
        );
    }

    @Test
    @DisplayName("좋아요를 정상적으로 추가한다")
    void addLike_ok() {
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

        PostLike postLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(postLikeRepository.existsByPostAndUser(post, user)).thenReturn(false);
        when(postLikeRepository.save(any(PostLike.class))).thenReturn(postLike);
        doNothing().when(postService).incrementLikeCount(post, userId);

        // When
        PostLikeDto result = postLikeService.addLike(userId, postId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.likeCount()).isEqualTo(0); // incrementLikeCount 호출 전이므로 0
        verify(postService, times(1)).findByPostId(postId);
        verify(postLikeRepository, times(1)).save(any(PostLike.class));
        verify(postService, times(1)).incrementLikeCount(post, userId);
    }

    @Test
    @DisplayName("존재하지 않는 postId로 좋아요를 추가하려고 하면 TARGET_NOT_FOUND 예외를 던진다")
    void addLike_postNotFound_throws() {
        // Given
        long userId = 1L;
        long postId = 999L;

        when(postService.findByPostId(postId))
                .thenThrow(new DPlayException(ResponseError.TARGET_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postLikeService.addLike(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, never()).findById(anyLong());
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("존재하지 않는 userId로 좋아요를 추가하려고 하면 USER_NOT_FOUND 예외를 던진다")
    void addLike_userNotFound_throws() {
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
        assertThatThrownBy(() -> postLikeService.addLike(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, times(1)).findById(userId);
        verify(postLikeRepository, never()).save(any(PostLike.class));
    }

    @Test
    @DisplayName("이미 좋아요를 누른 경우 RESOURCE_ALREADY_EXISTS 예외를 던진다")
    void addLike_alreadyLiked_throws() {
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
                .likeCount(1)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(postLikeRepository.existsByPostAndUser(post, user)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> postLikeService.addLike(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.RESOURCE_ALREADY_EXISTS);

        verify(postLikeRepository, never()).save(any(PostLike.class));
        verify(postService, never()).incrementLikeCount(any(Post.class), anyLong());
    }

    @Test
    @DisplayName("좋아요를 정상적으로 해제한다")
    void removeLike_ok() {
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
                .likeCount(1)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        PostLike postLike = PostLike.builder()
                .post(post)
                .user(user)
                .build();

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(postLikeRepository.existsByPostAndUser(post, user)).thenReturn(true);
        when(postLikeRepository.findByPostAndUser(post, user)).thenReturn(java.util.Optional.of(postLike));
        doNothing().when(postLikeRepository).delete(postLike);
        doNothing().when(postService).decrementLikeCount(post, userId);

        // When
        PostLikeDto result = postLikeService.removeLike(userId, postId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.likeCount()).isEqualTo(1); // decrementLikeCount 호출 전이므로 1
        verify(postService, times(1)).findByPostId(postId);
        verify(postLikeRepository, times(1)).delete(postLike);
        verify(postService, times(1)).decrementLikeCount(post, userId);
    }

    @Test
    @DisplayName("존재하지 않는 postId로 좋아요를 해제하려고 하면 TARGET_NOT_FOUND 예외를 던진다")
    void removeLike_postNotFound_throws() {
        // Given
        long userId = 1L;
        long postId = 999L;

        when(postService.findByPostId(postId))
                .thenThrow(new DPlayException(ResponseError.TARGET_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postLikeService.removeLike(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, never()).findById(anyLong());
        verify(postLikeRepository, never()).delete(any(PostLike.class));
    }

    @Test
    @DisplayName("존재하지 않는 userId로 좋아요를 해제하려고 하면 USER_NOT_FOUND 예외를 던진다")
    void removeLike_userNotFound_throws() {
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
                .likeCount(1)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        when(postService.findByPostId(postId)).thenReturn(post);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postLikeService.removeLike(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userRepository, times(1)).findById(userId);
        verify(postLikeRepository, never()).delete(any(PostLike.class));
    }

    @Test
    @DisplayName("좋아요를 누르지 않은 경우 TARGET_NOT_FOUND 예외를 던진다")
    void removeLike_notLiked_throws() {
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
        when(postLikeRepository.existsByPostAndUser(post, user)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> postLikeService.removeLike(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postLikeRepository, never()).delete(any(PostLike.class));
        verify(postService, never()).decrementLikeCount(any(Post.class), anyLong());
    }

}


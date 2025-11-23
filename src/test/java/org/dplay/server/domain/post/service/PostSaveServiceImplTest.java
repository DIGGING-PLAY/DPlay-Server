package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.UserPostsResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.entity.PostSave;
import org.dplay.server.domain.post.repository.PostRepository;
import org.dplay.server.domain.post.repository.PostSaveRepository;
import org.dplay.server.domain.post.service.impl.PostSaveServiceImpl;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.service.UserService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostSaveServiceImplTest {

    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 11, 3);

    @Mock
    private PostSaveRepository postSaveRepository;
    @Mock
    private PostService postService;
    @Mock
    private UserService userService;
    @Mock
    private PostRepository postRepository;

    private PostSaveServiceImpl postSaveService;

    @BeforeEach
    void setUp() {
        postSaveService = new PostSaveServiceImpl(
                postSaveRepository,
                postService,
                postRepository,
                userService
        );
    }

    @Test
    @DisplayName("스크랩을 정상적으로 추가한다")
    void addScrap_ok() {
        // Given
        long userId = 1L;
        long postId = 1L;

        User user = User.builder()
                .platform(Platform.KAKAO)
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
        when(userService.getUserById(userId)).thenReturn(user);
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
        verify(userService, never()).getUserById(anyLong());
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
        when(userService.getUserById(userId))
                .thenThrow(new DPlayException(ResponseError.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postSaveService.addScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userService, times(1)).getUserById(userId);
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
        when(userService.getUserById(userId)).thenReturn(user);
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
        when(userService.getUserById(userId)).thenReturn(user);
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
        verify(userService, never()).getUserById(anyLong());
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
        when(userService.getUserById(userId))
                .thenThrow(new DPlayException(ResponseError.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postSaveService.removeScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(postService, times(1)).findByPostId(postId);
        verify(userService, times(1)).getUserById(userId);
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
        when(userService.getUserById(userId)).thenReturn(user);
        when(postSaveRepository.existsByPostAndUser(post, user)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> postSaveService.removeScrap(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postSaveRepository, never()).delete(any(PostSave.class));
        verify(postService, never()).decrementSaveCount(any(Post.class));
    }

    @Test
    @DisplayName("유저가 스크랩한 글 리스트를 정상적으로 조회한다")
    void getUserSaves_ok() {
        // Given
        Long userId = 1L;
        String cursor = null;
        Integer limit = 20;

        User user = User.builder()
                .platform(Platform.KAKAO)
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

        Track track1 = Track.builder()
                .trackId("apple:track001")
                .songTitle("노래1")
                .artistName("아티스트1")
                .coverImg("https://example.com/cover1.jpg")
                .build();

        Track track2 = Track.builder()
                .trackId("apple:track002")
                .songTitle("노래2")
                .artistName("아티스트2")
                .coverImg("https://example.com/cover2.jpg")
                .build();

        Post post1 = Post.builder()
                .user(user)
                .question(question)
                .track(track1)
                .content("첫 번째 스크랩")
                .likeCount(10)
                .saveCount(5)
                .build();
        ReflectionTestUtils.setField(post1, "postId", 1L);

        Post post2 = Post.builder()
                .user(user)
                .question(question)
                .track(track2)
                .content("두 번째 스크랩")
                .likeCount(5)
                .saveCount(2)
                .build();
        ReflectionTestUtils.setField(post2, "postId", 2L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(postSaveRepository.countByUserUserId(userId)).thenReturn(2L);
        when(postRepository.findSavedPostsByUserDesc(userId, null, 21)).thenReturn(List.of(post1, post2));

        // When
        UserPostsResultDto result = postSaveService.getUserSaves(userId, cursor, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.visibleLimit()).isEqualTo(20);
        assertThat(result.totalCount()).isEqualTo(2L);
        assertThat(result.nextCursor()).isNull();
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).getPostId()).isEqualTo(1L);
        assertThat(result.items().get(1).getPostId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("유저가 스크랩한 글 리스트 조회 시 커서를 사용한다")
    void getUserSaves_withCursor_ok() {
        // Given
        Long userId = 1L;
        String cursor = java.util.Base64.getEncoder().encodeToString("5".getBytes());
        Integer limit = 10;

        User user = User.builder()
                .platform(Platform.KAKAO)
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
                .trackId("apple:track001")
                .songTitle("노래1")
                .artistName("아티스트1")
                .coverImg("https://example.com/cover1.jpg")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("스크랩")
                .likeCount(10)
                .saveCount(5)
                .build();
        ReflectionTestUtils.setField(post, "postId", 3L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(postSaveRepository.countByUserUserId(userId)).thenReturn(10L);
        when(postRepository.findSavedPostsByUserDesc(userId, 5L, 11)).thenReturn(List.of(post));

        // When
        UserPostsResultDto result = postSaveService.getUserSaves(userId, cursor, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.visibleLimit()).isEqualTo(10);
        assertThat(result.totalCount()).isEqualTo(10L);
        assertThat(result.nextCursor()).isNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("유저가 스크랩한 글 리스트 조회 시 nextCursor가 생성된다")
    void getUserSaves_withNextCursor_ok() {
        // Given
        Long userId = 1L;
        String cursor = null;
        Integer limit = 2;

        User user = User.builder()
                .platform(Platform.KAKAO)
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

        Track track1 = Track.builder()
                .trackId("apple:track001")
                .songTitle("노래1")
                .artistName("아티스트1")
                .coverImg("https://example.com/cover1.jpg")
                .build();

        Track track2 = Track.builder()
                .trackId("apple:track002")
                .songTitle("노래2")
                .artistName("아티스트2")
                .coverImg("https://example.com/cover2.jpg")
                .build();

        Track track3 = Track.builder()
                .trackId("apple:track003")
                .songTitle("노래3")
                .artistName("아티스트3")
                .coverImg("https://example.com/cover3.jpg")
                .build();

        Post post1 = Post.builder()
                .user(user)
                .question(question)
                .track(track1)
                .content("첫 번째 스크랩")
                .likeCount(10)
                .saveCount(5)
                .build();
        ReflectionTestUtils.setField(post1, "postId", 1L);

        Post post2 = Post.builder()
                .user(user)
                .question(question)
                .track(track2)
                .content("두 번째 스크랩")
                .likeCount(5)
                .saveCount(2)
                .build();
        ReflectionTestUtils.setField(post2, "postId", 2L);

        Post post3 = Post.builder()
                .user(user)
                .question(question)
                .track(track3)
                .content("세 번째 스크랩")
                .likeCount(3)
                .saveCount(1)
                .build();
        ReflectionTestUtils.setField(post3, "postId", 3L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(postSaveRepository.countByUserUserId(userId)).thenReturn(10L);
        when(postRepository.findSavedPostsByUserDesc(userId, null, 3)).thenReturn(List.of(post1, post2, post3));

        // When
        UserPostsResultDto result = postSaveService.getUserSaves(userId, cursor, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.visibleLimit()).isEqualTo(2);
        assertThat(result.totalCount()).isEqualTo(10L);
        assertThat(result.nextCursor()).isNotNull();
        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0).getPostId()).isEqualTo(1L);
        assertThat(result.items().get(1).getPostId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("존재하지 않는 유저의 스크랩한 글 리스트를 조회하려고 하면 USER_NOT_FOUND 예외를 던진다")
    void getUserSaves_userNotFound_throws() {
        // Given
        Long userId = 999L;
        String cursor = null;
        Integer limit = 20;

        when(userService.getUserById(userId))
                .thenThrow(new DPlayException(ResponseError.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postSaveService.getUserSaves(userId, cursor, limit))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(userService, times(1)).getUserById(userId);
        verify(postSaveRepository, never()).countByUserUserId(any());
    }

    @Test
    @DisplayName("잘못된 커서를 사용하면 INVALID_REQUEST_PARAMETER 예외를 던진다")
    void getUserSaves_invalidCursor_throws() {
        // Given
        Long userId = 1L;
        String invalidCursor = "invalid_cursor";
        Integer limit = 20;

        User user = User.builder()
                .platform(Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        when(userService.getUserById(userId)).thenReturn(user);

        // When & Then
        assertThatThrownBy(() -> postSaveService.getUserSaves(userId, invalidCursor, limit))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.INVALID_REQUEST_PARAMETER);

        verify(userService, times(1)).getUserById(userId);
        verify(postSaveRepository, never()).countByUserUserId(any());
    }
}


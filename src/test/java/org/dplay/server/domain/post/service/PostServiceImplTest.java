package org.dplay.server.domain.post.service;

import org.dplay.server.domain.post.dto.PostDto;
import org.dplay.server.domain.post.dto.UserPostsResultDto;
import org.dplay.server.domain.post.entity.Post;
import org.dplay.server.domain.post.repository.PostLikeRepository;
import org.dplay.server.domain.post.repository.PostRepository;
import org.dplay.server.domain.post.repository.PostSaveRepository;
import org.dplay.server.domain.post.service.impl.PostServiceImpl;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.question.service.QuestionEditorPickService;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.service.TrackService;
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

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 11, 3);

    @Mock
    private PostRepository postRepository;
    @Mock
    private PostLikeRepository postLikeRepository;
    @Mock
    private PostSaveRepository postSaveRepository;
    @Mock
    private PostQueryService postQueryService;
    @Mock
    private QuestionEditorPickService questionEditorPickService;
    @Mock
    private TrackService trackService;
    @Mock
    private QuestionService questionService;
    @Mock
    private UserService userService;

    private PostServiceImpl postService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_DATE.atStartOfDay(ZONE).toInstant(), ZONE);
        postService = new PostServiceImpl(
                postRepository,
                postLikeRepository,
                postSaveRepository,
                postQueryService,
                questionEditorPickService,
                trackService,
                questionService,
                fixedClock,
                userService
        );
    }

    @Test
    @DisplayName("추천글을 정상적으로 등록한다")
    void createPost_ok() {
        // Given
        String trackId = "apple:1678382";
        String songTitle = "Blueming";
        String artistName = "IU";
        String coverImg = "https://example.com/cover.jpg";
        String isrc = "KRA381901710";
        String content = "이 노래 짱!";

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        Track track = Track.builder()
                .trackId(trackId)
                .songTitle(songTitle)
                .artistName(artistName)
                .coverImg(coverImg)
                .isrc(isrc)
                .build();

        Post savedPost = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content(content)
                .likeCount(0)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(savedPost, "postId", 1L);

        when(questionService.getQuestionByDate(FIXED_DATE)).thenReturn(question);
        when(userService.getUserById(1L)).thenReturn(user);
        when(trackService.createTrackByPost(trackId, songTitle, artistName, coverImg, isrc)).thenReturn(track);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // When
        PostDto result = postService.createPost(1L, trackId, songTitle, artistName, coverImg, isrc, content);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.postId()).isEqualTo(1L);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("Track이 없으면 새로 생성한다")
    void createPost_createsTrackWhenNotFound() {
        // Given
        String trackId = "apple:1678382";
        String songTitle = "Blueming";
        String artistName = "IU";
        String coverImg = "https://example.com/cover.jpg";
        String isrc = "KRA381901710";
        String content = "이 노래 짱!";

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", 1L);

        Track newTrack = Track.builder()
                .trackId(trackId)
                .songTitle(songTitle)
                .artistName(artistName)
                .coverImg(coverImg)
                .isrc(isrc)
                .build();

        Post savedPost = Post.builder()
                .user(user)
                .question(question)
                .track(newTrack)
                .content(content)
                .likeCount(0)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(savedPost, "postId", 1L);

        when(questionService.getQuestionByDate(FIXED_DATE)).thenReturn(question);
        when(userService.getUserById(1L)).thenReturn(user);
        when(trackService.createTrackByPost(trackId, songTitle, artistName, coverImg, isrc)).thenReturn(newTrack);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // When
        PostDto result = postService.createPost(1L, trackId, songTitle, artistName, coverImg, isrc, content);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.postId()).isEqualTo(1L);
        verify(trackService, times(1)).createTrackByPost(trackId, songTitle, artistName, coverImg, isrc);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("오늘의 질문이 없으면 QUESTION_NOT_FOUND 예외를 던진다")
    void createPost_questionNotFound_throws() {
        // Given
        String trackId = "apple:1678382";
        String songTitle = "Blueming";
        String artistName = "IU";
        String coverImg = "https://example.com/cover.jpg";
        String isrc = "KRA381901710";
        String content = "이 노래 짱!";

        when(questionService.getQuestionByDate(FIXED_DATE))
                .thenThrow(new DPlayException(ResponseError.QUESTION_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postService.createPost(1L, trackId, songTitle, artistName, coverImg, isrc, content))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.QUESTION_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자가 없으면 USER_NOT_FOUND 예외를 던진다")
    void createPost_userNotFound_throws() {
        // Given
        String trackId = "apple:1678382";
        String songTitle = "Blueming";
        String artistName = "IU";
        String coverImg = "https://example.com/cover.jpg";
        String isrc = "KRA381901710";
        String content = "이 노래 짱!";

        Question question = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(question, "questionId", 1L);

        when(questionService.getQuestionByDate(FIXED_DATE)).thenReturn(question);
        when(userService.getUserById(1L)).thenThrow(new DPlayException(ResponseError.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postService.createPost(1L, trackId, songTitle, artistName, coverImg, isrc, content))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("추천글을 정상적으로 삭제한다")
    void deletePostByPostId_ok() {
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

        when(postRepository.findById(postId)).thenReturn(java.util.Optional.of(post));
        doNothing().when(postRepository).delete(post);

        // When
        postService.deletePostByPostId(userId, postId);

        // Then
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("존재하지 않는 postId를 삭제하려고 하면 TARGET_NOT_FOUND 예외를 던진다")
    void deletePostByPostId_postNotFound_throws() {
        // Given
        long userId = 1L;
        long postId = 999L;

        when(postRepository.findById(postId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThatThrownBy(() -> postService.deletePostByPostId(userId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.TARGET_NOT_FOUND);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("다른 유저가 작성한 글을 삭제하려고 하면 FORBIDDEN_RESOURCE 예외를 던진다")
    void deletePostByPostId_forbiddenResource_throws() {
        // Given
        long requestUserId = 1L; // 삭제를 요청한 유저
        long postOwnerId = 2L;  // 실제 글 작성자
        long postId = 1L;

        User postOwner = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("글작성자")
                .build();
        ReflectionTestUtils.setField(postOwner, "userId", postOwnerId);

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
                .user(postOwner)  // 다른 유저가 작성한 글
                .question(question)
                .track(track)
                .content("이 노래 짱!")
                .likeCount(0)
                .saveCount(0)
                .build();
        ReflectionTestUtils.setField(post, "postId", postId);

        when(postRepository.findById(postId)).thenReturn(java.util.Optional.of(post));

        // When & Then
        assertThatThrownBy(() -> postService.deletePostByPostId(requestUserId, postId))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.FORBIDDEN_RESOURCE);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    @DisplayName("유저가 등록한 곡 리스트를 정상적으로 조회한다")
    void getUserPosts_ok() {
        // Given
        Long userId = 1L;
        String cursor = null;
        Integer limit = 20;

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
                .content("첫 번째 추천")
                .likeCount(10)
                .saveCount(5)
                .build();
        ReflectionTestUtils.setField(post1, "postId", 1L);

        Post post2 = Post.builder()
                .user(user)
                .question(question)
                .track(track2)
                .content("두 번째 추천")
                .likeCount(5)
                .saveCount(2)
                .build();
        ReflectionTestUtils.setField(post2, "postId", 2L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(postQueryService.countByUser(userId)).thenReturn(2L);
        when(postQueryService.findPostsByUserDesc(userId, null, 21)).thenReturn(List.of(post1, post2));

        // When
        UserPostsResultDto result = postService.getUserPosts(userId, cursor, limit);

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
    @DisplayName("유저가 등록한 곡 리스트 조회 시 커서를 사용한다")
    void getUserPosts_withCursor_ok() {
        // Given
        Long userId = 1L;
        String cursor = java.util.Base64.getEncoder().encodeToString("5".getBytes());
        Integer limit = 10;

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
                .trackId("apple:track001")
                .songTitle("노래1")
                .artistName("아티스트1")
                .coverImg("https://example.com/cover1.jpg")
                .build();

        Post post = Post.builder()
                .user(user)
                .question(question)
                .track(track)
                .content("추천")
                .likeCount(10)
                .saveCount(5)
                .build();
        ReflectionTestUtils.setField(post, "postId", 3L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(postQueryService.countByUser(userId)).thenReturn(10L);
        when(postQueryService.findPostsByUserDesc(userId, 5L, 11)).thenReturn(List.of(post));

        // When
        UserPostsResultDto result = postService.getUserPosts(userId, cursor, limit);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.visibleLimit()).isEqualTo(10);
        assertThat(result.totalCount()).isEqualTo(10L);
        assertThat(result.nextCursor()).isNull();
        assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("유저가 등록한 곡 리스트 조회 시 nextCursor가 생성된다")
    void getUserPosts_withNextCursor_ok() {
        // Given
        Long userId = 1L;
        String cursor = null;
        Integer limit = 2;

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
                .content("첫 번째 추천")
                .likeCount(10)
                .saveCount(5)
                .build();
        ReflectionTestUtils.setField(post1, "postId", 1L);

        Post post2 = Post.builder()
                .user(user)
                .question(question)
                .track(track2)
                .content("두 번째 추천")
                .likeCount(5)
                .saveCount(2)
                .build();
        ReflectionTestUtils.setField(post2, "postId", 2L);

        Post post3 = Post.builder()
                .user(user)
                .question(question)
                .track(track3)
                .content("세 번째 추천")
                .likeCount(3)
                .saveCount(1)
                .build();
        ReflectionTestUtils.setField(post3, "postId", 3L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(postQueryService.countByUser(userId)).thenReturn(10L);
        when(postQueryService.findPostsByUserDesc(userId, null, 3)).thenReturn(List.of(post1, post2, post3));

        // When
        UserPostsResultDto result = postService.getUserPosts(userId, cursor, limit);

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
    @DisplayName("존재하지 않는 유저의 곡 리스트를 조회하려고 하면 USER_NOT_FOUND 예외를 던진다")
    void getUserPosts_userNotFound_throws() {
        // Given
        Long userId = 999L;
        String cursor = null;
        Integer limit = 20;

        when(userService.getUserById(userId))
                .thenThrow(new DPlayException(ResponseError.USER_NOT_FOUND));

        // When & Then
        assertThatThrownBy(() -> postService.getUserPosts(userId, cursor, limit))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.USER_NOT_FOUND);

        verify(userService, times(1)).getUserById(userId);
        verify(postQueryService, never()).countByUser(any());
    }

    @Test
    @DisplayName("잘못된 커서를 사용하면 INVALID_REQUEST_PARAMETER 예외를 던진다")
    void getUserPosts_invalidCursor_throws() {
        // Given
        Long userId = 1L;
        String invalidCursor = "invalid_cursor";
        Integer limit = 20;

        User user = User.builder()
                .platform(org.dplay.server.domain.user.Platform.KAKAO)
                .platformId("123456")
                .nickname("테스트유저")
                .build();
        ReflectionTestUtils.setField(user, "userId", userId);

        when(userService.getUserById(userId)).thenReturn(user);

        // When & Then
        assertThatThrownBy(() -> postService.getUserPosts(userId, invalidCursor, limit))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.INVALID_REQUEST_PARAMETER);

        verify(userService, times(1)).getUserById(userId);
        verify(postQueryService, never()).countByUser(any());
    }
}


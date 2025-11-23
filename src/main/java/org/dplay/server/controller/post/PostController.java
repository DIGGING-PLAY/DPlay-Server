package org.dplay.server.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.post.dto.PostIdResponse;
import org.dplay.server.controller.post.dto.PostLikeResponse;
import org.dplay.server.controller.post.dto.PostRequest;
import org.dplay.server.controller.question.dto.PastRecommendationFeedRequest;
import org.dplay.server.controller.question.dto.PastRecommendationFeedResponse;
import org.dplay.server.controller.question.dto.TodayRecommendationFeedResponse;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.post.dto.PostDto;
import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.domain.post.dto.PostLikeDto;
import org.dplay.server.domain.post.service.PostFeedService;
import org.dplay.server.domain.post.service.PostLikeService;
import org.dplay.server.domain.post.service.PostSaveService;
import org.dplay.server.domain.post.service.PostService;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final PostLikeService postLikeService;
    private final PostSaveService postSaveService;
    private final PostFeedService postFeedService;
    private final AuthService authService;

    /**
     * [ 추천글 등록 API ]
     *
     * @param accessToken
     * @param request
     * @return PostResponse
     * @apiNote 1. 성공적으로 추천글을 등록했을 때
     * / 2. 오늘의 질문이 존재하지 않을 때, DPlayException QUESTION_NOT_FOUND 발생
     * / 3. 사용자가 존재하지 않을 때, DPlayException USER_NOT_FOUND 발생
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PostIdResponse>> createPost(
            @RequestHeader("Authorization") final String accessToken,
            @Valid @RequestBody PostRequest request
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        PostDto postDto = postService.createPost(
                userId,
                request.trackId(),
                request.songTitle(),
                request.artistName(),
                request.coverImg(),
                request.isrc(),
                request.content()
        );

        PostIdResponse response = PostIdResponse.of(postDto.postId());
        return ResponseBuilder.created(response);
    }

    /**
     * [ 추천글 삭제 API ]
     *
     * @param accessToken
     * @param postId
     * @return null
     * @apiNote 1. 성공적으로 추천글을 삭제했을 때
     * / 2. postId에 해당하는 추천글이 존재하지 않을 때, DPlayException TARGET_NOT_FOUND 발생
     * / 3. 사용자가 존재하지 않을 때, DPlayException USER_NOT_FOUND 발생
     * / 4. 사용자가 작성한 글이 아닐 때, DPlayExceptionFORBIDDEN_RESOURCE 발생
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("postId") final long postId
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        postService.deletePostByPostId(userId, postId);
        return ResponseBuilder.ok(null);
    }

    /**
     * [ 추천글 좋아요 등록 API ]
     *
     * @param accessToken
     * @param postId
     * @return PostLikeResponse
     * @apiNote 1. 성공적으로 좋아요를 추가했을 때
     * / 2. postId에 해당하는 추천글이 존재하지 않을 때, DPlayException TARGET_NOT_FOUND 발생
     * / 3. 사용자가 존재하지 않을 때, DPlayException USER_NOT_FOUND 발생
     * / 4. 이미 좋아요를 누른 경우, DPlayException RESOURCE_ALREADY_EXISTS 발생
     */
    @PostMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<PostLikeResponse>> addLike(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("postId") final long postId
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        PostLikeDto postLikeDto = postLikeService.addLike(userId, postId);
        PostLikeResponse response = PostLikeResponse.of(postLikeDto.likeCount());
        return ResponseBuilder.created(response);
    }

    /**
     * [ 추천글 좋아요 해제 API ]
     *
     * @param accessToken
     * @param postId
     * @return PostLikeResponse
     * @apiNote 1. 성공적으로 좋아요를 해제했을 때
     * / 2. postId에 해당하는 추천글이 존재하지 않을 때, DPlayException TARGET_NOT_FOUND 발생
     * / 3. 사용자가 존재하지 않을 때, DPlayException USER_NOT_FOUND 발생
     * / 4. 좋아요를 누르지 않은 경우, DPlayException TARGET_NOT_FOUND 발생
     */
    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<ApiResponse<PostLikeResponse>> removeLike(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("postId") final long postId
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        PostLikeDto postLikeDto = postLikeService.removeLike(userId, postId);
        PostLikeResponse response = PostLikeResponse.of(postLikeDto.likeCount());
        return ResponseBuilder.ok(response);
    }

    /**
     * [ 추천글 스크랩 등록 API ]
     *
     * @param accessToken
     * @param postId
     * @apiNote 1. 성공적으로 스크랩을 추가했을 때
     * / 2. postId에 해당하는 추천글이 존재하지 않을 때, DPlayException TARGET_NOT_FOUND 발생
     * / 3. 사용자가 존재하지 않을 때, DPlayException USER_NOT_FOUND 발생
     * / 4. 이미 스크랩을 한 경우, DPlayException RESOURCE_ALREADY_EXISTS 발생
     */
    @PostMapping("/{postId}/scraps")
    public ResponseEntity<ApiResponse<Void>> addScrap(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("postId") final long postId
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        postSaveService.addScrap(userId, postId);
        return ResponseBuilder.created(null);
    }

    /**
     * [ 추천글 스크랩 해제 API ]
     *
     * @param accessToken
     * @param postId
     * @apiNote 1. 성공적으로 스크랩을 해제했을 때
     * / 2. postId에 해당하는 추천글이 존재하지 않을 때, DPlayException TARGET_NOT_FOUND 발생
     * / 3. 사용자가 존재하지 않을 때, DPlayException USER_NOT_FOUND 발생
     * / 4. 스크랩을 하지 않은 경우, DPlayException TARGET_NOT_FOUND 발생
     */
    @DeleteMapping("/{postId}/scraps")
    public ResponseEntity<ApiResponse<Void>> removeScrap(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("postId") final long postId
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        postSaveService.removeScrap(userId, postId);
        return ResponseBuilder.ok(null);
    }

    /**
     * [ 과거 추천글 조회 API ]
     *
     * @param accessToken 인증 토큰
     * @param questionId  질문 ID
     * @param request     커서, 페이지 사이즈 정보
     * @return PastRecommendationFeedResponse
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<ApiResponse<PastRecommendationFeedResponse>> getPastRecommendationPosts(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("questionId") final Long questionId,
            @Valid @ModelAttribute final PastRecommendationFeedRequest request
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        PostFeedResultDto result = postFeedService.getPastRecommendationFeed(
                userId,
                questionId,
                request.cursor(),
                request.limit()
        );

        PastRecommendationFeedResponse response = PastRecommendationFeedResponse.from(result);
        return ResponseBuilder.ok(response);
    }

    /**
     * [ 오늘 추천글 조회 API ]
     *
     * @param accessToken 인증 토큰
     * @return TodayRecommendationFeedResponse
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<TodayRecommendationFeedResponse>> getTodayRecommendationPosts(
            @RequestHeader("Authorization") final String accessToken
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);

        PostFeedResultDto result = postFeedService.getTodayRecommendationFeed(userId);

        TodayRecommendationFeedResponse response = TodayRecommendationFeedResponse.from(result);
        return ResponseBuilder.ok(response);
    }
}

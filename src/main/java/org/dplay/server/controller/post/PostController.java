package org.dplay.server.controller.post;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.post.dto.PostRequest;
import org.dplay.server.controller.post.dto.PostResponse;
import org.dplay.server.domain.post.dto.PostDto;
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
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @RequestHeader("Authorization") final String accessToken,
            @Valid @RequestBody PostRequest request
    ) {
        // TODO: 추후 인증 구현 시 accessToken에서 userId 추출
        // 예: Long userId = authService.getUserIdFromToken(accessToken);
        Long userId = 2L; // DB에 있는 유저 ID

        PostDto postDto = postService.createPost(
                userId,
                request.trackId(),
                request.songTitle(),
                request.artistName(),
                request.coverImg(),
                request.isrc(),
                request.content()
        );

        PostResponse response = PostResponse.of(postDto.postId());
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
        // TODO: 추후 인증 구현 시 accessToken에서 userId 추출
        // 예: Long userId = authService.getUserIdFromToken(accessToken);
        Long userId = 2L; // DB에 있는 유저 ID

        postService.deletePostByPostId(userId, postId);
        return ResponseBuilder.ok(null);
    }
}

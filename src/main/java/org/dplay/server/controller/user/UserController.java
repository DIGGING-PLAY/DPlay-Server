package org.dplay.server.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.user.dto.ChangeProfileRequest;
import org.dplay.server.controller.user.dto.NotificationRequest;
import org.dplay.server.controller.user.dto.UserPostsRequest;
import org.dplay.server.controller.user.dto.UserPostsResponse;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.post.dto.UserPostsResultDto;
import org.dplay.server.domain.post.service.PostService;
import org.dplay.server.domain.user.service.UserService;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.annotations.NotNull;

import java.io.IOException;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final PostService postService;

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> changeProfile(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String accessToken,
            @Valid @RequestPart(required = false) ChangeProfileRequest changeProfileRequest,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) throws IOException {
        Long userId = authService.getUserIdFromToken(accessToken);

        userService.updateProfileImage(userId, profileImg);

        if (changeProfileRequest != null) {
            userService.updateNickname(userId, changeProfileRequest.nickname());
        }

        return ResponseBuilder.ok(null);
    }

    @PostMapping("/me/notifications")
    public ResponseEntity<ApiResponse<Void>> notification(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String accessToken,
            @Valid @RequestBody NotificationRequest notificationRequest
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);
        userService.updateNotification(userId, notificationRequest.pushOn());

        return ResponseBuilder.created(null);
    }

    /**
     * [ 유저가 등록한 곡 리스트 조회 API ]
     *
     * @param accessToken 인증 토큰
     * @param userId      유저 ID
     * @param request     커서, 페이지 사이즈 정보
     * @return UserPostsResponse
     * @apiNote 1. 성공적으로 유저가 등록한 곡 리스트를 조회했을 때
     * / 2. userId에 해당하는 유저가 존재하지 않을 때, DPlayException USER_NOT_FOUND 발생
     */
    @GetMapping("/{userId}/posts")
    public ResponseEntity<ApiResponse<UserPostsResponse>> getUserPosts(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String accessToken,
            @PathVariable("userId") final Long userId,
            @Valid @ModelAttribute final UserPostsRequest request
    ) {
        Long currentUserId = authService.getUserIdFromToken(accessToken);

        UserPostsResultDto result = postService.getUserPosts(
                userId,
                request.cursor(),
                request.limit()
        );

        boolean isHost = userId.equals(currentUserId);

        UserPostsResponse response = UserPostsResponse.from(result, isHost);

        return ResponseBuilder.ok(response);
    }
}

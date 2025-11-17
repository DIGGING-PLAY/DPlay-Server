package org.dplay.server.controller.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.user.dto.ChangeProfileRequest;
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

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> changeProfile(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String accessToken,
            @Valid @RequestPart(required = false) ChangeProfileRequest changeProfileRequest,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) throws IOException {
        userService.updateProfileImage(accessToken, profileImg);

        if (changeProfileRequest != null) {
            userService.updateNickname(accessToken, changeProfileRequest.nickname());
        }

        return ResponseBuilder.ok(null);
    }
}

package org.dplay.server.controller.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.controller.auth.dto.SignupRequest;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.s3.S3Service;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final S3Service s3Service;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtTokenResponse>> login(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String providerToken,
            @Valid @RequestBody Map<String, String> body
    ) {
        String platformStr = body.get("platform");
        Platform platform = Platform.from(platformStr);
        LoginRequest loginRequest = new LoginRequest(platform);

        return ResponseBuilder.ok(authService.login(providerToken, loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<JwtTokenResponse>> signup(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String providerToken,
            @Valid @RequestPart SignupRequest signupRequest,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) throws IOException {
        String profileImgUrl = (profileImg == null) ? null : s3Service.uploadImage(profileImg);

        JwtTokenResponse jwtTokenResponse = authService.signup(
                providerToken,
                signupRequest,
                profileImgUrl
        );

        return ResponseBuilder.created(jwtTokenResponse);
    }
}

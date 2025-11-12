package org.dplay.server.controller.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.controller.auth.dto.SignupRequest;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.global.auth.constant.Constant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String providerToken,
            @Valid @RequestBody Map<String, String> body
    ) {
        String platformStr = body.get("platform");
        Platform platform = Platform.from(platformStr);
        LoginRequest loginRequest = new LoginRequest(platform);

        return ResponseEntity.ok(authService.login(providerToken, loginRequest));
    }

    @PostMapping("/signup")
    public ResponseEntity<JwtTokenResponse> signup(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String providerToken,
            @Valid @RequestPart SignupRequest signupRequest,
            @RequestPart("profileImg") MultipartFile profileImg
    ) {
        JwtTokenResponse jwtTokenResponse = authService.signup(providerToken, signupRequest, profileImg);

        return ResponseEntity.status(HttpStatus.CREATED).body(jwtTokenResponse);
    }
}

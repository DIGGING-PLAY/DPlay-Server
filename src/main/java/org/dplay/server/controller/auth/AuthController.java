package org.dplay.server.controller.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.global.auth.constant.Constant;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtTokenResponse> login(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String providerToken,
            @Valid @RequestBody final LoginRequest loginRequest
    ) {
        return ResponseEntity.ok(authService.login(providerToken, loginRequest));
    }

}

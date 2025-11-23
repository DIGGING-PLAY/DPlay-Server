package org.dplay.server.domain.auth.service;

import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.controller.auth.dto.SignupRequest;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.user.Platform;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AuthService {
    JwtTokenResponse login(String providerToken, LoginRequest loginRequest);

    JwtTokenResponse signup(String providerToken, SignupRequest signupRequest, MultipartFile profileImg) throws IOException;

    JwtTokenResponse reissueToken(String refreshToken);

    SocialUserDto getSocialInfo(String providerToken, Platform platform);

    Long getUserIdFromToken(String accessToken);

    String getToken(String token);
}

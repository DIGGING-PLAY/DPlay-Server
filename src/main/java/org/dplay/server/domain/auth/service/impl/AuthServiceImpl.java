package org.dplay.server.domain.auth.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.controller.auth.dto.SignupRequest;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.openfeign.apple.service.AppleService;
import org.dplay.server.domain.auth.openfeign.kakao.service.KakaoService;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.s3.S3Service;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.service.UserService;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.auth.jwt.JwtTokenProvider;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.dplay.server.global.util.NicknameValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final S3Service s3Service;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final NicknameValidator nicknameValidator;

    @Transactional
    public JwtTokenResponse login(final String providerToken, final LoginRequest loginRequest) {
        String platformId = getSocialInfo(providerToken, loginRequest.platform()).platformId();
        boolean isRegistered = userService.existsByProviderIdAndProvider(platformId, loginRequest.platform());

        if (isRegistered) {
            User user = userService.findUserByProviderIdAndProvider(platformId, loginRequest.platform());
            JwtTokenResponse tokens = jwtTokenProvider.issueTokens(user.getUserId());
            user.updateRefreshToken(tokens.refreshToken());
            return tokens;
        } else {
            throw new DPlayException(ResponseError.USER_NOT_FOUND);
        }
    }

    @Transactional
    public JwtTokenResponse signup(final String providerToken, final SignupRequest signupRequest, final MultipartFile profileImg) throws IOException {
        String platformId = getSocialInfo(providerToken, signupRequest.platform()).platformId();

        if (userService.existsByProviderIdAndProvider(platformId, signupRequest.platform())) {
            throw new DPlayException(ResponseError.USER_ALREADY_EXISTS);
        }

        if (userService.existsByNickname(signupRequest.nickname())) {
            throw new DPlayException(ResponseError.RESOURCE_ALREADY_EXISTS);
        } else {
            nicknameValidator.validate(signupRequest.nickname());
        }

        User user = User.builder()
                .platformId(platformId)
                .platform(signupRequest.platform())
                .nickname(signupRequest.nickname())
                .profileImg((profileImg == null) ? null : s3Service.uploadImage(profileImg)).build();
        userService.save(user);

        JwtTokenResponse tokens = jwtTokenProvider.issueTokens(user.getUserId());
        user.updateRefreshToken(tokens.refreshToken());

        user.updateRefreshToken(tokens.refreshToken());

        return tokens;
    }

    public SocialUserDto getSocialInfo(final String providerToken, final Platform platform) {
        if (platform.toString().equals("KAKAO")) {
            return kakaoService.getSocialUserInfo(providerToken);
        } else if (platform.toString().equals("APPLE")) {
            return appleService.getSocialUserInfo(providerToken);
        } else {
            throw new DPlayException(ResponseError.INVALID_PLATFORM_TYPE);
        }
    }

    public Long getUserIdFromToken(final String accessToken) {
        return jwtTokenProvider.getUserIdFromJwt(accessToken.substring(Constant.BEARER_TOKEN_PREFIX.length()));
    }
}

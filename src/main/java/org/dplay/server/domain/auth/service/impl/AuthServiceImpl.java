package org.dplay.server.domain.auth.service.impl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.controller.auth.dto.SignupRequest;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.openfeign.apple.service.AppleService;
import org.dplay.server.domain.auth.openfeign.kakao.service.KakaoService;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.facade.UserFacade;
import org.dplay.server.domain.user.service.UserService;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.auth.jwt.JwtTokenProvider;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.dplay.server.global.util.NicknameValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final NicknameValidator nicknameValidator;
    private final UserFacade userFacade;

    @Override
    @Transactional
    public JwtTokenResponse login(final String providerToken, final LoginRequest loginRequest) {
        String platformId = getSocialInfo(providerToken, loginRequest.platform()).platformId();
        log.info("[LOGIN-MIDDLE-1] 소셜 로그인 정보 조회 성공. platformId={}", platformId);
        boolean isRegistered = userService.existsByProviderIdAndProvider(platformId, loginRequest.platform());
        log.info("[LOGIN-MIDDLE-2] 가입 여부 확인. isRegistered={}", isRegistered);

        if (isRegistered) {
            User user = userService.findUserByProviderIdAndProvider(platformId, loginRequest.platform());
            JwtTokenResponse tokens = jwtTokenProvider.issueTokens(user.getUserId());
            user.updateRefreshToken(tokens.refreshToken());
            log.info("[LOGIN-END] 로그인 성공. userId={}, accessTokenPrefix={}",
                    user.getUserId(), tokens.accessToken().substring(0, 10));
            return tokens;
        } else {
            log.warn("[LOGIN-FAIL] 등록되지 않은 사용자 platformId={}", platformId);
            throw new DPlayException(ResponseError.USER_NOT_FOUND);
        }
    }

    @Override
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

        User user = userService.makeUser(platformId, signupRequest.platform(), signupRequest.nickname(), profileImg);

        JwtTokenResponse tokens = jwtTokenProvider.issueTokens(user.getUserId());
        user.updateRefreshToken(tokens.refreshToken());

        return tokens;
    }

    @Override
    @Transactional
    public void withdraw(final String accessToken) {
        Long userId = getUserIdFromToken(accessToken);
        User user = userService.getUserById(userId);

        if (user.getPlatform().equals(Platform.KAKAO)) {
            kakaoService.unlinkKakaoUser(user.getPlatformId());
        } else if (user.getPlatform().equals(Platform.APPLE)) {
            appleService.revoke(user.getPlatformId());
        } else {
            throw new DPlayException(ResponseError.INVALID_PLATFORM_TYPE);
        }

        userFacade.deleteUser(userId);
    }

    @Override
    @Transactional
    public JwtTokenResponse reissueToken(final String refreshToken) {
        Long userId;
        String token = getToken(refreshToken);

        try {
            userId = jwtTokenProvider.getUserIdFromJwt(token);
        } catch (ExpiredJwtException e) {
            throw new DPlayException(ResponseError.EXPIRED_REFRESH_TOKEN);
        } catch (JwtException e) {
            throw new DPlayException(ResponseError.INVALID_REFRESH_TOKEN);
        }

        User user = userService.findByRefreshToken(getToken(refreshToken));
        if (!userId.equals(user.getUserId())) {
            throw new DPlayException(ResponseError.INVALID_REFRESH_TOKEN);
        }

        JwtTokenResponse tokens = jwtTokenProvider.issueTokens(userId);
        user.updateRefreshToken(tokens.refreshToken());
        return tokens;
    }

    @Override
    public SocialUserDto getSocialInfo(final String providerToken, final Platform platform) {
        if (platform.toString().equals("KAKAO")) {
            return kakaoService.getSocialUserInfo(providerToken);
        } else if (platform.toString().equals("APPLE")) {
            return appleService.getSocialUserInfo(providerToken);
        } else {
            throw new DPlayException(ResponseError.INVALID_PLATFORM_TYPE);
        }
    }

    @Override
    public Long getUserIdFromToken(final String accessToken) {
        return jwtTokenProvider.getUserIdFromJwt(getToken(accessToken));
    }

    @Override
    public String getToken(String token) {
        if (token.startsWith(Constant.BEARER_TOKEN_PREFIX)) {
            return token.substring(Constant.BEARER_TOKEN_PREFIX.length());
        } else {
            return token;
        }
    }
}

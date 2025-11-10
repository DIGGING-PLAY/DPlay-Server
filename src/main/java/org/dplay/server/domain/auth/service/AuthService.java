package org.dplay.server.domain.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.domain.auth.TokenSaver;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.entity.Token;
import org.dplay.server.domain.auth.openfeign.apple.service.AppleService;
import org.dplay.server.domain.auth.openfeign.kakao.service.KakaoService;
import org.dplay.server.domain.user.UserRetriever;
import org.dplay.server.domain.user.UserSaver;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.global.auth.jwt.JwtTokenProvider;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final UserRetriever userRetriever;
    private final UserSaver userSaver;
    private final TokenSaver tokenSaver;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public JwtTokenResponse login(final String providerToken, final LoginRequest loginRequest) {
        SocialUserDto socialUserDto = getSocialInfo(providerToken, loginRequest);
        boolean isRegistered = userRetriever.existsByProviderIdAndProvider(socialUserDto.platformId(), loginRequest.platform());

        if (isRegistered) {
            User user = userRetriever.findByProviderIdAndProvider(socialUserDto.platformId(), loginRequest.platform());
            JwtTokenResponse tokens = jwtTokenProvider.issueTokens(user.getUserId());
            tokenSaver.save(
                    Token.builder()
                            .id(user.getUserId())
                            .refreshToken(tokens.refreshToken())
                            .build()
            );
            return tokens;
        } else {
            throw new DPlayException(ResponseError.USER_NOT_FOUND);
        }
    }

    private SocialUserDto getSocialInfo(final String providerToken, final LoginRequest loginRequest) {
        if (loginRequest.platform().toString().equals("KAKAO")) {
            return kakaoService.getSocialUserInfo(providerToken);
        } else if (loginRequest.platform().toString().equals("APPLE")) {
            return appleService.getSocialUserInfo(providerToken);
        } else {
            throw new DPlayException(ResponseError.INVALID_PLATFORM_TYPE);
        }
    }
}

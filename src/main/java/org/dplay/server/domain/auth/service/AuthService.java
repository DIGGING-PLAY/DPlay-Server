package org.dplay.server.domain.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.controller.auth.dto.LoginRequest;
import org.dplay.server.controller.auth.dto.SignupRequest;
import org.dplay.server.domain.auth.TokenSaver;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.entity.Token;
import org.dplay.server.domain.auth.openfeign.apple.service.AppleService;
import org.dplay.server.domain.auth.openfeign.kakao.service.KakaoService;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.UserRetriever;
import org.dplay.server.domain.user.UserSaver;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.global.auth.jwt.JwtTokenProvider;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.regex.Pattern;

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
        SocialUserDto socialUserDto = getSocialInfo(providerToken, loginRequest.platform());
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

    @Transactional
    public JwtTokenResponse signup(final String providerToken, final SignupRequest signupRequest, final MultipartFile profileImg) {
        SocialUserDto socialUserDto = getSocialInfo(providerToken, signupRequest.platform());
        validateNickname(signupRequest.nickname());

        User user = User.builder()
                .platformId(socialUserDto.platformId())
                .platform(signupRequest.platform())
                .nickname(signupRequest.nickname())
                .profileImg(profileImg);
        userSaver.save(user);

        JwtTokenResponse tokens = jwtTokenProvider.issueTokens(user.getUserId());
        tokenSaver.save(Token.builder()
                .id(user.getUserId())
                .refreshToken(tokens.refreshToken())
                .build());

        return tokens;
    }

    private SocialUserDto getSocialInfo(final String providerToken, final Platform platform) {
        if (platform.toString().equals("KAKAO")) {
            return kakaoService.getSocialUserInfo(providerToken);
        } else if (platform.toString().equals("APPLE")) {
            return appleService.getSocialUserInfo(providerToken);
        } else {
            throw new DPlayException(ResponseError.INVALID_PLATFORM_TYPE);
        }
    }

    private void validateNickname(final String nickname) {
        if (nickname.length() < 2 || nickname.length() > 10) {
            throw new DPlayException(ResponseError.INVALID_INPUT_LENGTH);
        } else if (!Pattern.compile("^[가-힣a-zA-Z0-9]+$").matcher(nickname).matches()) {
            throw new DPlayException(ResponseError.INVALID_INPUT_NICKNAME);
        }
    }
}

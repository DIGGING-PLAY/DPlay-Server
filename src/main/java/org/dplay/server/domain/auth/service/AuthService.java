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
import org.dplay.server.domain.s3.S3Service;
import org.dplay.server.domain.user.Platform;
import org.dplay.server.domain.user.UserRetriever;
import org.dplay.server.domain.user.UserSaver;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.repository.UserRepository;
import org.dplay.server.global.auth.jwt.JwtTokenProvider;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.dplay.server.global.util.NicknameValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.dplay.server.global.auth.constant.Constant.BEARER_TOKEN_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final S3Service s3Service;
    private final UserRetriever userRetriever;
    private final UserSaver userSaver;
    private final TokenSaver tokenSaver;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final NicknameValidator nicknameValidator;

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
    public JwtTokenResponse signup(final String providerToken, final SignupRequest signupRequest, final MultipartFile profileImg) throws IOException {
        SocialUserDto socialUserDto = getSocialInfo(providerToken, signupRequest.platform());

        if (userRetriever.existsByProviderIdAndProvider(socialUserDto.platformId(), signupRequest.platform())) {
            throw new DPlayException(ResponseError.USER_ALREADY_EXISTS);
        }

        if (userRetriever.existsByNickname(signupRequest.nickname())) {
            throw new DPlayException(ResponseError.RESOURCE_ALREADY_EXISTS);
        } else {
            nicknameValidator.validate(signupRequest.nickname());
        }

        User user = User.builder()
                .platformId(socialUserDto.platformId())
                .platform(signupRequest.platform())
                .nickname(signupRequest.nickname())
                .profileImg((profileImg == null) ? null : s3Service.uploadImage(profileImg)).build();
        userSaver.save(user);

        JwtTokenResponse tokens = jwtTokenProvider.issueTokens(user.getUserId());
        tokenSaver.save(Token.builder()
                .id(user.getUserId())
                .refreshToken(tokens.refreshToken())
                .build());

        user.updateRefreshToken(tokens.refreshToken());

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

    public Long getUserIdFromToken(final String accessToken) {
        return jwtTokenProvider.getUserIdFromJwt(accessToken.replace(BEARER_TOKEN_PREFIX, ""));
    }

    public User getUserFromToken(final String accessToken) {
        Long userId = jwtTokenProvider.getUserIdFromJwt(accessToken.replace(BEARER_TOKEN_PREFIX, ""));

        return userRepository.findById(userId)
                .orElseThrow(() -> new DPlayException(ResponseError.USER_NOT_FOUND));
    }
}

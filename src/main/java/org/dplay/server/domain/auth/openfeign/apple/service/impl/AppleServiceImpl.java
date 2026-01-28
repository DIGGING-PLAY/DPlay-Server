package org.dplay.server.domain.auth.openfeign.apple.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.openfeign.apple.AppleFeignClient;
import org.dplay.server.domain.auth.openfeign.apple.dto.ApplePublicKeys;
import org.dplay.server.domain.auth.openfeign.apple.dto.AppleTokenDto;
import org.dplay.server.domain.auth.openfeign.apple.service.AppleService;
import org.dplay.server.domain.auth.openfeign.apple.verify.AppleClientSecretGenerator;
import org.dplay.server.domain.auth.openfeign.apple.verify.AppleJwtParser;
import org.dplay.server.domain.auth.openfeign.apple.verify.PublicKeyGenerator;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleServiceImpl implements AppleService {

    private final AppleFeignClient appleFeignClient;
    private final AppleJwtParser appleJwtParser;
    private final PublicKeyGenerator publicKeyGenerator;
    private final AppleClientSecretGenerator appleClientSecretGenerator;

    @Value("${oauth.apple.client-id}")
    private String clientId;

    public SocialUserDto getSocialUserInfo(String identityToken) {

        Map<String, String> headers = appleJwtParser.parseHeaders(identityToken);
        ApplePublicKeys applePublicKeys = appleFeignClient.getApplePublicKeys();
        PublicKey publicKey = publicKeyGenerator.generatePublicKey(headers, applePublicKeys);
        Claims claims = appleJwtParser.parsePublicKeyAndGetClaims(identityToken, publicKey);
        return SocialUserDto.of(claims.get("sub", String.class));
    }

    @Override
    public void revoke(String authCode) {
        log.info("[APPLE-REVOKE-START] 애플 회원 탈퇴 시작");
        try {
            String clientSecret = appleClientSecretGenerator.createClientSecret();
            log.info("[APPLE-REVOKE-MIDDLE-1] clientSecret 생성 완료");

            String refreshToken = getRefreshToken(authCode, clientSecret);
            log.info("[APPLE-REVOKE-MIDDLE-2] refreshToken 발급 완료");

            appleFeignClient.revoke(
                    clientId,
                    clientSecret,
                    refreshToken,
                    "refresh_token"
            );
            log.info("[APPLE-REVOKE-END] 애플 회원 탈퇴 성공");

        } catch (Exception e) {
            log.error("[APPLE-REVOKE-FAIL] 애플 회원 탈퇴 실패", e);
            throw new DPlayException(ResponseError.APPLE_REVOKE_FAILED);
        }
    }

    private String getRefreshToken(final String authCode, final String clientSecret) {
        log.info("[APPLE-TOKEN-START] 애플 토큰 요청 시작");

        try {
            AppleTokenDto appleTokenDto = appleFeignClient.getAppleToken(
                    clientId,
                    clientSecret,
                    "authorization_code",
                    authCode
            );

            log.info("[APPLE-TOKEN-END] 애플 토큰 요청 성공");
            return appleTokenDto.refreshToken();
        } catch (Exception e) {
            log.error("[APPLE-TOKEN-FAIL] 애플 토큰 요청 실패", e);
            throw new DPlayException(ResponseError.APPLE_TOKEN_REQUEST_FAILED);
        }
    }
}
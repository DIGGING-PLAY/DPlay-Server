package org.dplay.server.domain.auth.openfeign.apple.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
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
        try {
            String clientSecret = appleClientSecretGenerator.createClientSecret();
            String refreshToken = getRefreshToken(authCode, clientSecret);
            appleFeignClient.revoke(
                    clientId,
                    clientSecret,
                    refreshToken,
                    "refresh_token"
            );
        } catch (Exception e) {
            throw new DPlayException(ResponseError.APPLE_REVOKE_FAILED);
        }
    }

    private String getRefreshToken(final String authCode, final String clientSecret) {
        try {
            AppleTokenDto appleTokenDto = appleFeignClient.getAppleToken(
                    clientId,
                    clientSecret,
                    "authorization_code",
                    authCode
            );
            return appleTokenDto.refreshToken();
        } catch (Exception e) {
            throw new DPlayException(ResponseError.APPLE_TOKEN_REQUEST_FAILED);
        }
    }
}
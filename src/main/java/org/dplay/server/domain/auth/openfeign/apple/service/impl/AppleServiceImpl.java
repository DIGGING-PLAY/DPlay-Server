package org.dplay.server.domain.auth.openfeign.apple.service.impl;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.dto.SocialUserDto;
import org.dplay.server.domain.auth.openfeign.apple.AppleFeignClient;
import org.dplay.server.domain.auth.openfeign.apple.dto.ApplePublicKeys;
import org.dplay.server.domain.auth.openfeign.apple.service.AppleService;
import org.dplay.server.domain.auth.openfeign.apple.verify.AppleJwtParser;
import org.dplay.server.domain.auth.openfeign.apple.verify.PublicKeyGenerator;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppleServiceImpl implements AppleService {

    private final AppleFeignClient appleFeignClient;
    private final AppleJwtParser appleJwtParser;
    private final PublicKeyGenerator publicKeyGenerator;

    public SocialUserDto getSocialUserInfo(String identityToken) {

        Map<String, String> headers = appleJwtParser.parseHeaders(identityToken);
        ApplePublicKeys applePublicKeys = appleFeignClient.getApplePublicKeys();
        PublicKey publicKey = publicKeyGenerator.generatePublicKey(headers, applePublicKeys);
        Claims claims = appleJwtParser.parsePublicKeyAndGetClaims(identityToken, publicKey);
        return SocialUserDto.of(claims.get("sub", String.class));
    }

}
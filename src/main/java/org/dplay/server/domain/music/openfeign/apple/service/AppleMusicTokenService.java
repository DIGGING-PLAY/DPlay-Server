package org.dplay.server.domain.music.openfeign.apple.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Service
public class AppleMusicTokenService {

    private final String teamId;
    private final String keyId;
    private final String privateKeyPath;
    private final long tokenExpiration;

    public AppleMusicTokenService(
            @Value("${apple.music.team-id}") String teamId,
            @Value("${apple.music.key-id}") String keyId,
            @Value("${apple.music.private-key-path}") String privateKeyPath,
            @Value("${apple.music.token-expiration}") long tokenExpiration
    ) {
        this.teamId = teamId;
        this.keyId = keyId;
        this.privateKeyPath = privateKeyPath;
        this.tokenExpiration = tokenExpiration;
    }

    /**
     * Apple Music Developer Token 생성 (JWT)
     * Apple Music API 인증에 사용되는 토큰입니다.
     */
    public String generateDeveloperToken() {
        try {
            String privateKeyContent = readPrivateKeyFile();
            PrivateKey privateKey = parsePrivateKey(privateKeyContent);
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(tokenExpiration);

            return Jwts.builder()
                    .setHeaderParam("alg", "ES256")
                    .setHeaderParam("kid", keyId)
                    .setIssuer(teamId)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .signWith(privateKey, SignatureAlgorithm.ES256)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate Apple Music developer token", e);
            throw new RuntimeException("Failed to generate Apple Music developer token", e);
        }
    }

    /**
     * resources 폴더에서 .p8 파일을 읽어옵니다.
     */
    private String readPrivateKeyFile() throws IOException {
        ClassPathResource resource = new ClassPathResource(privateKeyPath);
        if (!resource.exists()) {
            throw new IOException("Private key file not found: " + privateKeyPath);
        }
        return new String(resource.getInputStream().readAllBytes());
    }

    private PrivateKey parsePrivateKey(String privateKeyContent) throws Exception {
        // PEM 형식의 private key를 파싱
        String privateKeyPEM = privateKeyContent
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePrivate(keySpec);
    }
}

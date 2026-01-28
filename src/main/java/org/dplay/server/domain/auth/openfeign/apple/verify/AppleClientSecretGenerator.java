package org.dplay.server.domain.auth.openfeign.apple.verify;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class AppleClientSecretGenerator {

    private static final String SIGN_ALGORITHM_HEADER_KEY = "alg";
    private static final String KEY_ID_HEADER_KEY = "kid";
    private static final String AUDIENCE = "https://appleid.apple.com";

    @Value("${oauth.apple.client-id}")
    private String clientId;

    @Value("${oauth.apple.key-id}")
    private String keyId;

    @Value("${oauth.apple.team-id}")
    private String teamId;

    @Value("${oauth.apple.key-file-path}")
    private String keyFilePath;

    public String createClientSecret() throws IOException {
        try {
            Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
            Map<String, Object> jwtHeader = new HashMap<>();
            jwtHeader.put(KEY_ID_HEADER_KEY, keyId); // kid
            jwtHeader.put(SIGN_ALGORITHM_HEADER_KEY, SignatureAlgorithm.ES256); // alg
            String privateKeyContent = readPrivateKeyFile();
            PrivateKey privateKey = parsePrivateKey(privateKeyContent);

            return Jwts.builder()
                    .setHeaderParams(jwtHeader)
                    .setIssuer(teamId) // iss
                    .setIssuedAt(new Date(System.currentTimeMillis())) // 발행 시간
                    .setExpiration(expirationDate) // 만료 시간
                    .setAudience(AUDIENCE) // aud
                    .setSubject(clientId) // sub
                    .signWith(SignatureAlgorithm.ES256, privateKey)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Apple Music developer token", e);
        }
    }

    private String readPrivateKeyFile() throws IOException {
        Path path = Paths.get(keyFilePath);

        if (!Files.exists(path)) {
            throw new IOException("Private key file not found: " + path.toAbsolutePath());
        }

        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private PrivateKey parsePrivateKey(String privateKeyContent) throws Exception {
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

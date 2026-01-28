package org.dplay.server.domain.auth.openfeign.apple.verify;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
        Map<String, Object> jwtHeader = new HashMap<>();
        jwtHeader.put(KEY_ID_HEADER_KEY, keyId); // kid
        jwtHeader.put(SIGN_ALGORITHM_HEADER_KEY, SignatureAlgorithm.ES256); // alg
        return Jwts.builder()
                .setHeaderParams(jwtHeader)
                .setIssuer(teamId) // iss
                .setIssuedAt(new Date(System.currentTimeMillis())) // 발행 시간
                .setExpiration(expirationDate) // 만료 시간
                .setAudience(AUDIENCE) // aud
                .setSubject(clientId) // sub
                .signWith(SignatureAlgorithm.ES256, getPrivateKey())
                .compact();
    }

    public PrivateKey getPrivateKey() throws IOException {
        String privateKeyContent = readPrivateKeyFile();

        try (Reader pemReader = new StringReader(privateKeyContent);
             PEMParser pemParser = new PEMParser(pemReader)) {

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            PrivateKeyInfo keyInfo = (PrivateKeyInfo) pemParser.readObject();
            return converter.getPrivateKey(keyInfo);
        }
    }

    private String readPrivateKeyFile() throws IOException {
        Path path = Paths.get(keyFilePath);
        if (!Files.exists(path)) {
            throw new IOException("Private key file not found: " + path.toAbsolutePath());
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}

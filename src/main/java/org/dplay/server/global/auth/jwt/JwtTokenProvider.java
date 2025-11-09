package org.dplay.server.global.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.dplay.server.controller.auth.dto.JwtTokenResponse;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtTokenProvider implements InitializingBean {
    @Value("${jwt.access-expiration}")
    private Long accessTokenExpirationTime;
    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenExpirationTime;
    @Value("${jwt.secret-key}")
    private String secretKey;

    private Key signingKey;

    @Override
    public void afterPropertiesSet() throws Exception {
        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.signingKey = Keys.hmacShaKeyFor(encodedKey.getBytes());
    }

    public JwtTokenResponse issueTokens(Long userId) {
        return JwtTokenResponse.of(
                userId,
                generateToken(userId, true),
                generateToken(userId, false));
    }

    public String generateToken(Long userId, boolean isAccessToken) {
        final Date now = new Date();
        final Date expirationDate = generateExpirationDate(now, isAccessToken);
        final Claims claims = Jwts.claims()
                .setIssuedAt(now)
                .setExpiration(expirationDate);

        claims.put(Constant.USER_ID, userId);

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims)
                .signWith(signingKey)
                .compact();
    }

    private Date generateExpirationDate(Date now, boolean isAccessToken) {
        if (isAccessToken) {
            return new Date(now.getTime() + accessTokenExpirationTime);
        }
        return new Date(now.getTime() + refreshTokenExpirationTime);
    }

    public Claims getBody(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getUserIdFromJwt(String token) {
        Claims claims = getBody(token);
        return Long.valueOf(claims.get(Constant.USER_ID).toString());
    }

    public static Object checkPrincipal(final Object principal) {
        if ("anonymousUser".equals(principal)) {
            throw new DPlayException(ResponseError.USER_NOT_FOUND);
        }
        return principal;
    }
}

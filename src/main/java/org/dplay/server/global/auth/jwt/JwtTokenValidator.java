package org.dplay.server.global.auth.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.RequiredArgsConstructor;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenValidator {

    private final JwtTokenProvider jwtTokenProvider;

    public void validateAccessToken(String accessToken) {
        try {
            jwtTokenProvider.getBody(accessToken);
        } catch (MalformedJwtException ex) {
            throw new DPlayException(ResponseError.INVALID_ACCESS_TOKEN);
        } catch (ExpiredJwtException ex) {
            throw new DPlayException(ResponseError.EXPIRED_ACCESS_TOKEN);
        } catch (UnsupportedJwtException | IllegalArgumentException ex) {
            throw new DPlayException(ResponseError.INVALID_TOKEN);
        }
    }
}

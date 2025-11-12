package org.dplay.server.domain.auth;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.auth.entity.Token;
import org.dplay.server.domain.auth.repository.TokenRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenSaver {

    private final TokenRepository tokenRepository;

    public Token save(final Token token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        return tokenRepository.save(token);
    }

}

package org.dplay.server.domain.auth.repository;

import org.dplay.server.domain.auth.entity.Token;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface TokenRepository extends CrudRepository<Token, Long> {

    Optional<Token> findIdByRefreshToken(String refreshToken);
}

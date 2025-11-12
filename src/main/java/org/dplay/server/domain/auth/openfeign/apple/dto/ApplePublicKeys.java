package org.dplay.server.domain.auth.openfeign.apple.dto;

import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;

import java.util.List;

public record ApplePublicKeys(List<ApplePublicKey> keys) {
    public ApplePublicKey getMatchesKey(String alg, String kid) {
        return this.keys
                .stream()
                .filter(k -> k.alg().equals(alg) && k.kid().equals(kid))
                .findFirst()
                .orElseThrow(() -> new DPlayException(ResponseError.INVALID_TOKEN));
    }
}

package org.dplay.server.domain.auth.openfeign.apple.dto;

public record ApplePublicKey(
        String kty, String kid, String use, String alg, String n, String e
) {

}

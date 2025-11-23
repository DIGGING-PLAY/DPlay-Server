package org.dplay.server.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;

public enum Platform {
    KAKAO,
    APPLE;

    @JsonCreator
    public static Platform from(String value) {
        try {
            return Platform.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new DPlayException(ResponseError.INVALID_PLATFORM_TYPE);
        }
    }
}

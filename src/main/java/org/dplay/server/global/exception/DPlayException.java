package org.dplay.server.global.exception;

import lombok.Getter;
import org.dplay.server.global.response.ResponseError;

@Getter
public class DPlayException extends RuntimeException {
    private final ResponseError responseError;

    public DPlayException(ResponseError responseError) {
        super(responseError.getMessage());
        this.responseError = responseError;
    }
}

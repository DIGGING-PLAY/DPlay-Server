package org.dplay.server.global.util;

import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class NicknameValidator {

    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[가-힣a-zA-Z0-9]+$");

    public void validate(final String nickname) {
        if (nickname.length() < 2 || nickname.length() > 10) {
            throw new DPlayException(ResponseError.INVALID_INPUT_LENGTH);
        } else if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new DPlayException(ResponseError.INVALID_INPUT_NICKNAME);
        }
    }
}

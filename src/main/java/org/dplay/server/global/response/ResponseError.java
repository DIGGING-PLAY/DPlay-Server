package org.dplay.server.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseError {
    // Bad Request (400)
    BAD_REQUEST(4000, "잘못된 요청입니다."),
    INVALID_PLATFORM_TYPE(4001, "유효하지 않은 플랫폼 타입입니다."),
    INVALID_REQUEST_PARAMETER(4002, "요청 파라미터가 잘못되었습니다."),
    INVALID_INPUT_LENGTH(4003, "입력된 글자수가 허용된 범위를 벗어났습니다."),
    INVALID_INPUT_NICKNAME(4004, "닉네임 형식이 잘못되었습니다."),
    INVALID_GRANT(4005, "유효하지 않은 인가 코드입니다."),

    // Unauthorized (401)
    INVALID_ACCESS_TOKEN(4010, "액세스 토큰의 값이 올바르지 않습니다."),
    EXPIRED_ACCESS_TOKEN(4011, "액세스 토큰이 만료되었습니다. 재발급 받아주세요."),
    INVALID_TOKEN(4012, "토큰 값이 올바르지 않습니다."),

    // Forbidden (403)
    FORBIDDEN_RESOURCE(4030, "리소스 접근 권한이 없습니다."),

    // Not Found (404)
    TARGET_NOT_FOUND(4040, "대상을 찾을 수 없습니다."),

    // Method Not Allowed (405)
    METHOD_NOT_ALLOWED(4050, "잘못된 HTTP method 요청입니다."),

    // Conflict (409)
    RESOURCE_ALREADY_EXISTS(4090, "이미 존재하는 리소스입니다."),

    // Internal Server Error (500)
    INTERNAL_SERVER_ERROR(5000, "서버 내부 오류입니다.");

    private final int code;
    private final String message;

    ResponseError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.valueOf(code / 10);
    }
}



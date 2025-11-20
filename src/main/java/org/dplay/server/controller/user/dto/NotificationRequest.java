package org.dplay.server.controller.user.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull(message = "pushOn은 필수 값입니다.")
        boolean pushOn
) {
}

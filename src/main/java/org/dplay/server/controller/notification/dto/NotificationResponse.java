package org.dplay.server.controller.notification.dto;

import jakarta.validation.constraints.NotNull;

public record NotificationResponse(
        @NotNull(message = "pushOn은 필수 값입니다.")
        boolean pushOn
) {
}

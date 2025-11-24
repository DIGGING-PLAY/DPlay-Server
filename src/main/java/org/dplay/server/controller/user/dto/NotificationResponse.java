package org.dplay.server.controller.user.dto;

import org.dplay.server.domain.user.dto.NotificationDto;

public record NotificationResponse(
        boolean pushOn
) {
    public static NotificationResponse from(NotificationDto notificationDto) {
        return new NotificationResponse(notificationDto.notification());
    }
}

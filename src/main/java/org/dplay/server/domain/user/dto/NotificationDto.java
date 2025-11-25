package org.dplay.server.domain.user.dto;

import org.dplay.server.domain.user.entity.User;

public record NotificationDto(
        boolean notification
) {
    public static NotificationDto from(User user) {
        return new NotificationDto(user.isPushOn());
    }
}

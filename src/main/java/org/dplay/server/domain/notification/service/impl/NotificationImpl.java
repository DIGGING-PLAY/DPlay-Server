package org.dplay.server.domain.notification.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.notification.service.NotificationService;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationImpl implements NotificationService {

    private final UserService userService;

    @Override
    @Transactional
    public void updateNotification(Long userId, Boolean pushOn) {
        User user = userService.getUserById(userId);
        user.updatePushOptIn(pushOn);
    }
}

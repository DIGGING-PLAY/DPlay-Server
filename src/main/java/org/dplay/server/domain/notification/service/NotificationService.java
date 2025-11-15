package org.dplay.server.domain.notification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.user.entity.User;
import org.dplay.server.domain.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserService userService;

    @Transactional
    public void updateNotification(Long userId, Boolean pushOn) {
        User user = userService.getUserById(userId);
        user.updatePushOptIn(pushOn);
    }
}

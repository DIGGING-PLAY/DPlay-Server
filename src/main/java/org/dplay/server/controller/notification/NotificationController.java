package org.dplay.server.controller.notification;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.user.dto.NotificationRequest;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.notification.service.NotificationService;
import org.dplay.server.global.auth.constant.Constant;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("v1/me")
@RequiredArgsConstructor
public class NotificationController {

    private final AuthService authService;
    private final NotificationService notificationService;

    @PostMapping("notifications")
    public ResponseEntity<ApiResponse<Void>> notification(
            @NotNull @RequestHeader(Constant.AUTHORIZATION_HEADER) final String accessToken,
            @RequestBody NotificationRequest notificationRequest
    ) {
        Long userId = authService.getUserIdFromToken(accessToken);
        notificationService.updateNotification(userId, notificationRequest.pushOn());

        return ResponseBuilder.created(null);
    }
}

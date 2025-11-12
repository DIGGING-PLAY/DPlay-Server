package org.dplay.server.controller.auth.dto;

import org.dplay.server.domain.user.Platform;

public record LoginRequest(
        Platform platform
) {
}

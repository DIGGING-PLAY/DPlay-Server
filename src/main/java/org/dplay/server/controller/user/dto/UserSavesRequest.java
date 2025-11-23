package org.dplay.server.controller.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;

public record UserSavesRequest(
        String cursor,
        @Positive
        @Max(100)
        Integer limit
) {
}

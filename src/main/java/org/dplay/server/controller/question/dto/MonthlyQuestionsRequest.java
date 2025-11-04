package org.dplay.server.controller.question.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record MonthlyQuestionsRequest(
        @Min(value = 2000, message = "연도는 2025 이상이어야 합니다")
        @Max(value = 9999, message = "연도는 9999 이하여야 합니다")
        int year,

        @Min(value = 1, message = "월은 1 이상이어야 합니다")
        @Max(value = 12, message = "월은 12 이하여야 합니다")
        int month
) {
}

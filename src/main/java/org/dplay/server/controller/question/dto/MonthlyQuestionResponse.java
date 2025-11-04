package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.question.dto.QuestionDto;
import org.dplay.server.global.util.DateTimeFormatUtil;

public record MonthlyQuestionResponse(
        String day,
        long questionId,
        String title
) {
    public static MonthlyQuestionResponse of(QuestionDto dto) {
        return new MonthlyQuestionResponse(
                DateTimeFormatUtil.formatDayOfMonth(dto.displayDate()),
                dto.questionId(),
                dto.title()
        );
    }
}
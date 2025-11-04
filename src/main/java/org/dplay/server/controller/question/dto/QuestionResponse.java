package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.question.dto.QuestionDto;
import org.dplay.server.global.util.DateTimeFormatUtil;

public record QuestionResponse(
        long questionId,
        String date,
        String title
) {
    public static QuestionResponse of(QuestionDto dto) {
        return new QuestionResponse(
                dto.questionId(),
                DateTimeFormatUtil.formatDate(dto.displayDate()),
                dto.title()
        );
    }
}

package org.dplay.server.domain.question.dto;

import org.dplay.server.domain.question.entity.Question;

import java.time.LocalDate;

public record QuestionDto(
        long questionId,
        LocalDate displayDate,
        String title
) {
    public static QuestionDto of(Question question) {
        return new QuestionDto(
                question.getQuestionId(),
                question.getDisplayDate(),
                question.getTitle()
        );
    }
}

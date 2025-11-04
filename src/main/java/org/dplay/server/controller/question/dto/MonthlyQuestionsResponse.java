package org.dplay.server.controller.question.dto;

import org.dplay.server.domain.question.dto.QuestionDto;

import java.util.List;

public record MonthlyQuestionsResponse(
        List<MonthlyQuestionResponse> questions
) {
    public static MonthlyQuestionsResponse of(List<QuestionDto> dto) {
        List<MonthlyQuestionResponse> questions = dto.stream()
                .map(MonthlyQuestionResponse::of)
                .toList();
        return new MonthlyQuestionsResponse(questions);
    }
}

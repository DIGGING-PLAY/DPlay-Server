package org.dplay.server.domain.question.service;

import org.dplay.server.domain.question.dto.QuestionDto;

import java.util.List;

public interface QuestionService {
    QuestionDto getTodayQuestion();

    List<QuestionDto> getMonthlyQuestions(int year, int month);
}

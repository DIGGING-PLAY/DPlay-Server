package org.dplay.server.domain.question.service;

import org.dplay.server.domain.question.dto.QuestionDto;
import org.dplay.server.domain.question.entity.Question;

import java.time.LocalDate;
import java.util.List;

public interface QuestionService {
    QuestionDto getTodayQuestion();

    List<QuestionDto> getMonthlyQuestions(final int year, final int month);

    Question getQuestionByDate(LocalDate date);
}

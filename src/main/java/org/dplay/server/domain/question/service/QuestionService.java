package org.dplay.server.domain.question.service;

import org.dplay.server.domain.question.dto.QuestionDto;

import java.time.LocalDate;

public interface QuestionService {
    QuestionDto getTodayQuestion();

    QuestionDto getQuestionByDate(LocalDate date);
}

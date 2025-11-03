package org.dplay.server.domain.question.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.question.dto.QuestionDto;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.question.repository.QuestionRepository;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

    private final Clock clock;
    private final QuestionRepository questionRepository;

    @Override
    public QuestionDto getTodayQuestion() {
        LocalDate today = LocalDate.now(clock);
        return getQuestionByDate(today);
    }

    @Override
    public QuestionDto getQuestionByDate(LocalDate date) {
        Question question = questionRepository.findByDisplayDate(date)
                .orElseThrow(() -> new DPlayException(ResponseError.QUESTION_NOT_FOUND));
        return QuestionDto.of(question);
    }
}

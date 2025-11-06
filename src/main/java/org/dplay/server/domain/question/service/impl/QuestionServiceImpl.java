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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

    private final Clock clock;
    private final QuestionRepository questionRepository;

    @Override
    public QuestionDto getTodayQuestion() {
        Question question = getQuestionByDate(getTodayDate());
        return QuestionDto.of(question);
    }

    @Override
    public Question getQuestionByDate(LocalDate date) {
        return questionRepository.findByDisplayDate(date)
                .orElseThrow(() -> new DPlayException(ResponseError.QUESTION_NOT_FOUND));
    }

    @Override
    public List<QuestionDto> getMonthlyQuestions(final int year, final int month) {
        return getQuestionsByYearAndMonth(year, month, getTodayDate());
    }

    private List<QuestionDto> getQuestionsByYearAndMonth(final int year, final int month, LocalDate date) {
        int currentYear = date.getYear();
        int currentMonth = date.getMonthValue();

        if (year > currentYear || (year == currentYear && month > currentMonth)) {
            throw new DPlayException(ResponseError.FORBIDDEN_RESOURCE);
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate monthEndDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        LocalDate endDate = (year == currentYear && month == currentMonth)
                ? date
                : monthEndDate;

        List<Question> questions = questionRepository.findByDisplayDateBetweenOrderByDisplayDateAsc(startDate, endDate);

        if (questions.isEmpty()) {
            throw new DPlayException(ResponseError.QUESTION_NOT_FOUND);
        }

        return questions.stream()
                .map(QuestionDto::of)
                .collect(Collectors.toList());
    }

    private LocalDate getTodayDate() {
        return LocalDate.now(clock);
    }


}

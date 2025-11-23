package org.dplay.server.domain.question.service;

import org.dplay.server.domain.question.dto.QuestionDto;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.question.repository.QuestionRepository;
import org.dplay.server.domain.question.service.impl.QuestionServiceImpl;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 11, 3);

    @Mock
    private QuestionRepository questionRepository;

    private QuestionServiceImpl questionService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(FIXED_DATE.atStartOfDay(ZONE).toInstant(), ZONE);
        questionService = new QuestionServiceImpl(fixedClock, questionRepository);
    }

    @Test
    @DisplayName("오늘 질문을 정상 조회한다")
    void getTodayQuestion_ok() {
        Question q = Question.builder()
                .title("11월 3일에 듣고 싶은 노래는?")
                .displayDate(FIXED_DATE)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(q, "questionId", 1L);

        when(questionRepository.findByDisplayDate(FIXED_DATE)).thenReturn(Optional.of(q));

        QuestionDto dto = questionService.getTodayQuestion();

        assertThat(dto).isNotNull();
        assertThat(dto.questionId()).isEqualTo(1L);
        assertThat(dto.displayDate()).isEqualTo(FIXED_DATE);
        assertThat(dto.title()).contains("11월 3일");
    }

    @Test
    @DisplayName("오늘 질문이 없으면 예외를 던진다")
    void getTodayQuestion_notFound_throws() {
        when(questionRepository.findByDisplayDate(FIXED_DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.getTodayQuestion())
                .isInstanceOf(DPlayException.class)
                .hasMessageContaining(ResponseError.QUESTION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("과거 월의 질문을 정상 조회한다")
    void getMonthlyQuestions_pastMonth_ok() {
        // Given: FIXED_DATE 기준으로 이전 달 질문 조회
        LocalDate pastMonth = FIXED_DATE.minusMonths(1);
        int pastYear = pastMonth.getYear();
        int pastMonthValue = pastMonth.getMonthValue();

        LocalDate startDate = LocalDate.of(pastYear, pastMonthValue, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        LocalDate question1Date = LocalDate.of(pastYear, pastMonthValue, 1);
        LocalDate question2Date = LocalDate.of(pastYear, pastMonthValue, 15);

        Question q1 = Question.builder()
                .title("과거 월 1일 질문")
                .displayDate(question1Date)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(q1, "questionId", 1L);

        Question q2 = Question.builder()
                .title("과거 월 15일 질문")
                .displayDate(question2Date)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(q2, "questionId", 2L);

        when(questionRepository.findByDisplayDateBetweenOrderByDisplayDateAsc(startDate, endDate))
                .thenReturn(List.of(q1, q2));

        // When
        List<QuestionDto> result = questionService.getMonthlyQuestions(pastYear, pastMonthValue);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).questionId()).isEqualTo(1L);
        assertThat(result.get(0).displayDate()).isEqualTo(question1Date);
        assertThat(result.get(1).questionId()).isEqualTo(2L);
        assertThat(result.get(1).displayDate()).isEqualTo(question2Date);
    }

    @Test
    @DisplayName("현재 월의 질문을 정상 조회한다 (오늘 날짜까지만)")
    void getMonthlyQuestions_currentMonth_ok() {
        // Given: FIXED_DATE가 11월 3일이고, 11월 조회 시도 (11월 1일 ~ 11월 3일까지만 조회)
        int currentYear = FIXED_DATE.getYear();
        int currentMonth = FIXED_DATE.getMonthValue();

        LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate endDate = FIXED_DATE; // 오늘 날짜까지만

        LocalDate question1Date = LocalDate.of(currentYear, currentMonth, 1);
        LocalDate question2Date = LocalDate.of(currentYear, currentMonth, 2);

        Question q1 = Question.builder()
                .title("현재 월 1일 질문")
                .displayDate(question1Date)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(q1, "questionId", 1L);

        Question q2 = Question.builder()
                .title("현재 월 2일 질문")
                .displayDate(question2Date)
                .postCount(0)
                .build();
        ReflectionTestUtils.setField(q2, "questionId", 2L);

        when(questionRepository.findByDisplayDateBetweenOrderByDisplayDateAsc(startDate, endDate))
                .thenReturn(List.of(q1, q2));

        // When
        List<QuestionDto> result = questionService.getMonthlyQuestions(currentYear, currentMonth);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).questionId()).isEqualTo(1L);
        assertThat(result.get(0).displayDate()).isEqualTo(question1Date);
        assertThat(result.get(1).questionId()).isEqualTo(2L);
        assertThat(result.get(1).displayDate()).isEqualTo(question2Date);
    }

    @Test
    @DisplayName("미래 월에 접근하면 FORBIDDEN_RESOURCE 예외를 던진다")
    void getMonthlyQuestions_futureMonth_throwsForbidden() {
        // Given: FIXED_DATE 기준으로 다음 달 질문 조회 시도
        int currentYear = FIXED_DATE.getYear();
        int currentMonth = FIXED_DATE.getMonthValue();
        int futureMonth = currentMonth == 12 ? 1 : currentMonth + 1;
        int futureYear = currentMonth == 12 ? currentYear + 1 : currentYear;

        // When & Then
        assertThatThrownBy(() -> questionService.getMonthlyQuestions(futureYear, futureMonth))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.FORBIDDEN_RESOURCE);
    }

    @Test
    @DisplayName("다음 해의 질문에 접근하면 FORBIDDEN_RESOURCE 예외를 던진다")
    void getMonthlyQuestions_nextYear_throwsForbidden() {
        // Given: 현재가 2025년 11월 3일이고, 다음 해(2026년) 1월 질문 조회 시도
        int nextYear = FIXED_DATE.getYear() + 1;

        // When & Then
        assertThatThrownBy(() -> questionService.getMonthlyQuestions(nextYear, 1))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.FORBIDDEN_RESOURCE);
    }

    @Test
    @DisplayName("과거/현재 월인데 질문이 없으면 QUESTION_NOT_FOUND 예외를 던진다")
    void getMonthlyQuestions_noQuestions_throwsNotFound() {
        // Given: FIXED_DATE 기준으로 이전 달 질문이 없음
        LocalDate pastMonth = FIXED_DATE.minusMonths(1);
        int pastYear = pastMonth.getYear();
        int pastMonthValue = pastMonth.getMonthValue();

        LocalDate startDate = LocalDate.of(pastYear, pastMonthValue, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        when(questionRepository.findByDisplayDateBetweenOrderByDisplayDateAsc(startDate, endDate))
                .thenReturn(List.of());

        // When & Then
        assertThatThrownBy(() -> questionService.getMonthlyQuestions(pastYear, pastMonthValue))
                .isInstanceOf(DPlayException.class)
                .extracting("responseError")
                .isEqualTo(ResponseError.QUESTION_NOT_FOUND);
    }
}



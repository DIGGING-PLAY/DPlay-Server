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
}



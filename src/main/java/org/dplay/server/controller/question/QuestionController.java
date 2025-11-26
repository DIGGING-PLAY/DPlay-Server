package org.dplay.server.controller.question;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.question.dto.MonthlyQuestionsRequest;
import org.dplay.server.controller.question.dto.MonthlyQuestionsResponse;
import org.dplay.server.controller.question.dto.QuestionResponse;
import org.dplay.server.domain.auth.service.AuthService;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final AuthService authService;

    /**
     * [ 오늘의 질문을 조회하는 API ]
     *
     * @param accessToken
     * @return QuestionResponse
     * @throws DPlayException QUESTION_NOT_FOUND 발생
     * @apiNote 1. 성공적으로 오늘의 질문을 조회했을 때
     * / 2. 오늘의 질문이 DB에 존재하지 않을 때, DPlayException QUESTION_NOT_FOUND 발생
     */
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<QuestionResponse>> getTodayQuestion(
            @RequestHeader("Authorization") final String accessToken
    ) {
        authService.getUserIdFromToken(accessToken);
        QuestionResponse response = QuestionResponse.of(questionService.getTodayQuestion());
        return ResponseBuilder.ok(response);
    }

    /**
     * [ 과거의 질문을 월별로 조회하는 API ]
     *
     * @param accessToken
     * @param request
     * @return MonthlyQuestionsResponse
     * @ throws DPlayException QUESTION_NOT_FOUND 발생
     * @apiNote 1. 성공적으로 과거의 질문을 월별로 조회했을 때
     * / 2. 조회한 날짜에 질문이 DB에 존재하지 않을 때, DPlayException 404 QUESTION_NOT_FOUND 발생
     * / 3. 아직 공개되지 않은 미래의 질문을 조회했을 때, DPlayException 403 FORBIDDEN_RESOURCE 발생
     * / 4. 유효하지 않은 날짜 형식을 파라미터로 보냈을 때, DPlayException 400 INVALID_DATE_TYPE 발생
     */
    @GetMapping
    public ResponseEntity<ApiResponse<MonthlyQuestionsResponse>> getMonthlyQuestions(
            @RequestHeader("Authorization") final String accessToken,
            @Valid @ModelAttribute MonthlyQuestionsRequest request
    ) {
        authService.getUserIdFromToken(accessToken);
        MonthlyQuestionsResponse response = MonthlyQuestionsResponse.of(
                questionService.getMonthlyQuestions(request.year(), request.month())
        );
        return ResponseBuilder.ok(response);
    }
}
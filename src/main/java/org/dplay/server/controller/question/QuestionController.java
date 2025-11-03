package org.dplay.server.controller.question;

import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.question.dto.QuestionResponse;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

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
        Long userId = 1L; // TODO : 회원가입 이후 수정 예정
        QuestionResponse response = QuestionResponse.of(questionService.getTodayQuestion());
        return ResponseBuilder.ok(response);
    }
}
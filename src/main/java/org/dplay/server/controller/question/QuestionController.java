package org.dplay.server.controller.question;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.question.dto.*;
import org.dplay.server.domain.post.dto.PostFeedResultDto;
import org.dplay.server.domain.post.service.PostFeedService;
import org.dplay.server.domain.question.service.QuestionService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;

@RestController
@RequestMapping("/v1/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final PostFeedService postFeedService;
    private final Clock clock;

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
        Long userId = 1L; // TODO : 회원가입 이후 수정 예정
        MonthlyQuestionsResponse response = MonthlyQuestionsResponse.of(
                questionService.getMonthlyQuestions(request.year(), request.month())
        );
        return ResponseBuilder.ok(response);
    }

    /**
     * [ 과거 추천글 조회 API ]
     *
     * @param accessToken 인증 토큰 (TODO: 인증 연동 시 userId 추출)
     * @param questionId  질문 ID
     * @param request     커서, 페이지 사이즈 정보
     * @return PastRecommendationFeedResponse
     */
    @GetMapping("/{questionId}/posts")
    public ResponseEntity<ApiResponse<PastRecommendationFeedResponse>> getPastRecommendationPosts(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("questionId") final Long questionId,
            @Valid @ModelAttribute final PastRecommendationFeedRequest request
    ) {
        Long userId = 2L; // TODO: 추후 인증 구현 시 accessToken 에서 userId 추출

        PostFeedResultDto result = postFeedService.getPastRecommendationFeed(
                userId,
                questionId,
                request.cursor(),
                request.limit()
        );

        PastRecommendationFeedResponse response = PastRecommendationFeedResponse.from(result);
        return ResponseBuilder.ok(response);
    }

    /**
     * [ 오늘 추천글 조회 API ]
     */
    @GetMapping("/today/posts")
    public ResponseEntity<ApiResponse<TodayRecommendationFeedResponse>> getTodayRecommendationPosts(
            @RequestHeader("Authorization") final String accessToken
    ) {
        Long userId = 2L; // TODO: 추후 인증 구현 시 accessToken 에서 userId 추출

        LocalDate today = LocalDate.now(clock);

        PostFeedResultDto result = postFeedService.getTodayRecommendationFeed(
                userId,
                today
        );

        TodayRecommendationFeedResponse response = TodayRecommendationFeedResponse.from(result);
        return ResponseBuilder.ok(response);
    }
}
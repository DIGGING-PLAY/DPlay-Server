package org.dplay.server.controller.track;

import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.track.dto.TrackSearchResponse;
import org.dplay.server.domain.track.dto.TrackSearchResultDto;
import org.dplay.server.domain.track.service.TrackService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ApiResponse;
import org.dplay.server.global.response.ResponseBuilder;
import org.dplay.server.global.response.ResponseError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tracks")
@RequiredArgsConstructor
public class TrackController {
    private final TrackService trackService;

    /**
     * [ 음악 검색 API ]
     *
     * @param accessToken 인증 토큰
     * @param query       검색 키워드
     * @param limit       결과 수 제한
     * @param storefront  국가 코드
     * @param cursor      다음 페이지 커서
     * @return TrackSearchResponse
     * @apiNote Apple Music API를 통해 음악을 검색합니다.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<TrackSearchResponse>> searchTracks(
            @RequestHeader("Authorization") final String accessToken,
            @RequestParam("query") String query,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "storefront", required = false) String storefront,
            @RequestParam(value = "cursor", required = false) String cursor
    ) {
        // query 검증
        if (query == null || query.trim().isEmpty()) {
            throw new DPlayException(ResponseError.INVALID_REQUEST_PARAMETER);
        }
        if (query.length() > 100) {
            throw new DPlayException(ResponseError.INVALID_INPUT_LENGTH);
        }
        // TODO: 추후 인증 구현 시 accessToken에서 userId 추출
        // 예: Long userId = authService.getUserIdFromToken(accessToken);

        TrackSearchResultDto result = trackService.searchTracks(
                query,
                limit,
                storefront,
                cursor
        );

        TrackSearchResponse response = TrackSearchResponse.of(
                result.query(),
                result.storefront(),
                result.limit(),
                result.nextCursor(),
                result.items()
        );

        return ResponseBuilder.ok(response);
    }
}

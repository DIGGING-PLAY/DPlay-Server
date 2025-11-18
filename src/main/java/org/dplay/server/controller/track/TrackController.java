package org.dplay.server.controller.track;

import lombok.RequiredArgsConstructor;
import org.dplay.server.controller.track.dto.TrackDetailResponse;
import org.dplay.server.controller.track.dto.TrackPreviewResponse;
import org.dplay.server.controller.track.dto.TrackSearchResponse;
import org.dplay.server.domain.track.dto.TrackDetailResultDto;
import org.dplay.server.domain.track.dto.TrackPreviewResultDto;
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

    /**
     * [ 음악 상세 조회 API ]
     *
     * @param accessToken 인증 토큰
     * @param trackId     트랙 ID (apple:{appleMusicId} 형식)
     * @param storefront  국가 코드
     * @return TrackDetailResponse
     * @apiNote Apple Music API를 통해 특정 트랙의 상세 정보를 조회합니다.
     */
    @GetMapping("/{trackId}")
    public ResponseEntity<ApiResponse<TrackDetailResponse>> getTrackDetail(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("trackId") String trackId,
            @RequestParam(value = "storefront", required = false) String storefront
    ) {
        // trackId 검증
        if (trackId == null || trackId.trim().isEmpty() || !trackId.startsWith("apple:")) {
            throw new DPlayException(ResponseError.INVALID_REQUEST_PARAMETER);
        }

        // TODO: 추후 인증 구현 시 accessToken에서 userId 추출
        // 예: Long userId = authService.getUserIdFromToken(accessToken);

        TrackDetailResultDto result = trackService.getTrackDetail(trackId, storefront);

        TrackDetailResponse response = TrackDetailResponse.from(result);

        return ResponseBuilder.ok(response);
    }

    /**
     * [ 음악 미리듣기 API ]
     *
     * @param accessToken 인증 토큰
     * @param trackId     트랙 ID (apple:{appleMusicId} 형식)
     * @param storefront  국가 코드
     * @return TrackPreviewResponse
     * @apiNote Apple Music API를 통해 트랙의 30초 미리듣기 URL을 조회합니다.
     */
    @GetMapping("/preview/{trackId}")
    public ResponseEntity<ApiResponse<TrackPreviewResponse>> getPreview(
            @RequestHeader("Authorization") final String accessToken,
            @PathVariable("trackId") String trackId,
            @RequestParam(value = "storefront", required = false) String storefront
    ) {
        // trackId 검증
        if (trackId == null || trackId.trim().isEmpty() || !trackId.startsWith("apple:")) {
            throw new DPlayException(ResponseError.INVALID_REQUEST_PARAMETER);
        }

        // TODO: 추후 인증 구현 시 accessToken에서 userId 추출
        // 예: Long userId = authService.getUserIdFromToken(accessToken);

        TrackPreviewResultDto result = trackService.getPreview(trackId, storefront);

        // 미리듣기 URL이 없는 경우 에러 반환 (지역에 따라 제공되지 않을 수 있음)
        if (result.streamUrl() == null) {
            throw new DPlayException(ResponseError.PREVIEW_URL_NOT_AVAILABLE);
        }

        TrackPreviewResponse response = TrackPreviewResponse.from(result);

        return ResponseBuilder.ok(response);
    }
}

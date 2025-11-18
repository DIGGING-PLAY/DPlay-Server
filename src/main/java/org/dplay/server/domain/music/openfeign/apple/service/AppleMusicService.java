package org.dplay.server.domain.music.openfeign.apple.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.music.dto.MusicSearchItemDto;
import org.dplay.server.domain.music.openfeign.apple.AppleMusicFeignClient;
import org.dplay.server.domain.music.openfeign.apple.dto.AppleMusicSearchResponse;
import org.dplay.server.domain.music.openfeign.apple.dto.AppleMusicTrackData;
import org.dplay.server.domain.music.openfeign.apple.dto.AppleMusicTrackDetailResponse;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppleMusicService {

    private final AppleMusicFeignClient appleMusicFeignClient;
    private final AppleMusicTokenService appleMusicTokenService;

    /**
     * Apple Music API를 통해 음악 검색
     *
     * @param query      검색 키워드
     * @param limit      결과 수 제한
     * @param storefront 국가 코드 (기본값: kr)
     * @param offset     페이지 오프셋
     * @return 검색 결과와 다음 커서
     */
    public MusicSearchResult searchMusic(String query, Integer limit, String storefront, Integer offset) {
        String developerToken = appleMusicTokenService.generateDeveloperToken();
        String authorization = "Bearer " + developerToken;

        try {
            AppleMusicSearchResponse response = appleMusicFeignClient.search(
                    authorization,
                    query,
                    "songs",
                    limit,
                    offset,
                    storefront
            );

            if (response == null || response.results() == null ||
                    response.results().songs() == null ||
                    response.results().songs().data() == null) {
                return new MusicSearchResult(new ArrayList<>(), null);
            }

            List<MusicSearchItemDto> items = response.results().songs().data().stream()
                    .map(trackData -> {
                        var attrs = trackData.attributes();
                        String trackId = "apple:" + trackData.id();
                        String coverImg = attrs.artwork() != null ? attrs.artwork().getCoverImageUrl() : null;
                        String previewUrl = attrs.previews() != null && !attrs.previews().isEmpty()
                                ? attrs.previews().get(0).url()
                                : null;

                        return MusicSearchItemDto.builder()
                                .trackId(trackId)
                                .songTitle(attrs.name())
                                .artistName(attrs.artistName())
                                .coverImg(coverImg)
                                .isrc(attrs.isrc())
                                .previewUrl(previewUrl)
                                .build();
                    })
                    .toList();

            String nextCursor = response.results().songs().next() != null
                    ? encodeCursor(offset != null ? offset + limit : limit)
                    : null;

            return new MusicSearchResult(items, nextCursor);
        } catch (Exception e) {
            log.error("Failed to search Apple Music", e);
            throw new RuntimeException("Failed to search Apple Music: " + e.getMessage(), e);
        }
    }

    private String encodeCursor(int currentOffset) {
        try {
            // offset 정보를 Base64로 인코딩하여 커서 생성
            String cursorData = String.valueOf(currentOffset);
            return Base64.getEncoder().encodeToString(cursorData.getBytes());
        } catch (Exception e) {
            log.warn("Failed to encode cursor", e);
            return null;
        }
    }

    /**
     * Apple Music API를 통해 트랙 상세 정보 조회
     *
     * @param trackId    트랙 ID (apple:{appleMusicId} 형식)
     * @param storefront 국가 코드 (기본값: kr)
     * @return 트랙 상세 정보
     */
    public MusicTrackDetailResult getTrackDetail(String trackId, String storefront) {
        if (storefront == null || storefront.isBlank()) {
            storefront = "kr";
        }
        String developerToken = appleMusicTokenService.generateDeveloperToken();
        String authorization = "Bearer " + developerToken;

        // trackId에서 appleMusicId 추출 (apple:{appleMusicId} 형식)
        String appleMusicId = extractAppleMusicId(trackId);
        if (appleMusicId == null) {
            throw new DPlayException(ResponseError.INVALID_REQUEST_PARAMETER);
        }

        try {
            AppleMusicTrackDetailResponse response = appleMusicFeignClient.getTrackDetail(
                    authorization,
                    storefront,
                    appleMusicId
            );

            if (response == null || response.data() == null || response.data().isEmpty()) {
                throw new DPlayException(ResponseError.TARGET_NOT_FOUND);
            }

            AppleMusicTrackData trackData = response.data().get(0);
            var attrs = trackData.attributes();
            // 고해상도 이미지 URL 생성 (1024x1024)
            String coverImg = attrs.artwork() != null ? getHighResolutionCoverImageUrl(attrs.artwork()) : null;

            return new MusicTrackDetailResult(
                    trackId,
                    attrs.name(),
                    attrs.artistName(),
                    coverImg,
                    attrs.isrc()
            );
        } catch (DPlayException e) {
            // DPlayException은 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("Failed to get track detail from Apple Music (trackId: {})", trackId, e);
            throw new DPlayException(ResponseError.TARGET_NOT_FOUND);
        }
    }

    /**
     * trackId에서 Apple Music ID 추출
     * 형식: apple:{appleMusicId}
     *
     * @param trackId 트랙 ID
     * @return Apple Music ID (ID가 비어있으면 null 반환)
     */
    private String extractAppleMusicId(String trackId) {
        if (trackId == null || !trackId.startsWith("apple:")) {
            return null;
        }
        String id = trackId.substring(6); // "apple:".length() = 6
        return id.isEmpty() ? null : id;
    }

    /**
     * 고해상도 앨범 커버 이미지 URL 생성 (1024x1024)
     *
     * @param artwork Apple Music Artwork 객체
     * @return 고해상도 이미지 URL
     */
    private String getHighResolutionCoverImageUrl(org.dplay.server.domain.music.openfeign.apple.dto.AppleMusicArtwork artwork) {
        if (artwork == null || artwork.url() == null) {
            return null;
        }
        return artwork.url().replace("{w}", "1024").replace("{h}", "1024");
    }

    public record MusicSearchResult(
            List<MusicSearchItemDto> items,
            String nextCursor
    ) {
    }

    public record MusicTrackDetailResult(
            String trackId,
            String songTitle,
            String artistName,
            String coverImg,
            String isrc
    ) {
    }
}

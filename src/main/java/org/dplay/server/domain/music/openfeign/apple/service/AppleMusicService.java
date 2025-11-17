package org.dplay.server.domain.music.openfeign.apple.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.music.dto.MusicSearchItemDto;
import org.dplay.server.domain.music.openfeign.apple.AppleMusicFeignClient;
import org.dplay.server.domain.music.openfeign.apple.dto.AppleMusicSearchResponse;
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

    public record MusicSearchResult(
            List<MusicSearchItemDto> items,
            String nextCursor
    ) {
    }
}

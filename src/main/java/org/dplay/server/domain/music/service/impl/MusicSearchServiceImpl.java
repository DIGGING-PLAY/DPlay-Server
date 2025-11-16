package org.dplay.server.domain.music.service.impl;

import lombok.RequiredArgsConstructor;
import org.dplay.server.domain.music.openfeign.apple.service.AppleMusicService;
import org.dplay.server.domain.music.service.MusicSearchService;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class MusicSearchServiceImpl implements MusicSearchService {

    private final AppleMusicService appleMusicService;
    private static final String DEFAULT_STOREFRONT = "kr";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    @Override
    public MusicSearchResult search(String query, Integer limit, String storefront, String cursor) {
        // 파라미터 검증 및 기본값 설정
        String finalStorefront = storefront != null && !storefront.isEmpty() ? storefront : DEFAULT_STOREFRONT;
        int finalLimit = limit != null ? Math.min(Math.max(limit, 1), MAX_LIMIT) : DEFAULT_LIMIT;

        // 커서에서 offset 추출
        Integer offset = parseOffsetFromCursor(cursor);

        // Apple Music API 호출
        AppleMusicService.MusicSearchResult result = appleMusicService.searchMusic(
                query,
                finalLimit,
                finalStorefront,
                offset
        );

        return new MusicSearchResult(
                query,
                finalStorefront,
                finalLimit,
                result.nextCursor(),
                result.items()
        );
    }

    private Integer parseOffsetFromCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Integer.parseInt(decoded);
        } catch (Exception e) {
            return null;
        }
    }
}

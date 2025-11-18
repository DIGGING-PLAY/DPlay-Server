package org.dplay.server.domain.music.service;

import org.dplay.server.domain.music.dto.MusicSearchItemDto;

import java.util.List;

public interface MusicSearchService {
    MusicSearchResult search(String query, Integer limit, String storefront, String cursor);

    record MusicSearchResult(
            String query,
            String storefront,
            Integer limit,
            String nextCursor,
            List<MusicSearchItemDto> items
    ) {
    }
}

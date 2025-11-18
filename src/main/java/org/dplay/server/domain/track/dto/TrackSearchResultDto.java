package org.dplay.server.domain.track.dto;

import org.dplay.server.domain.music.dto.MusicSearchItemDto;

import java.util.List;

public record TrackSearchResultDto(
        String query,
        String storefront,
        Integer limit,
        String nextCursor,
        List<MusicSearchItemDto> items
) {
    public static TrackSearchResultDto of(
            String query,
            String storefront,
            Integer limit,
            String nextCursor,
            List<MusicSearchItemDto> items
    ) {
        return new TrackSearchResultDto(
                query,
                storefront,
                limit,
                nextCursor,
                items
        );
    }
}

package org.dplay.server.controller.track.dto;

import org.dplay.server.domain.music.dto.MusicSearchItemDto;

import java.util.List;

public record TrackSearchResponse(
        String query,
        String storefront,
        Integer limit,
        String nextCursor,
        List<TrackSearchItemResponse> items
) {
    public static TrackSearchResponse of(
            String query,
            String storefront,
            Integer limit,
            String nextCursor,
            List<MusicSearchItemDto> items
    ) {
        List<TrackSearchItemResponse> itemResponses = items.stream()
                .map(TrackSearchItemResponse::from)
                .toList();

        return new TrackSearchResponse(
                query,
                storefront,
                limit,
                nextCursor,
                itemResponses
        );
    }

    public record TrackSearchItemResponse(
            String trackId,
            String songTitle,
            String artistName,
            String coverImg,
            String isrc
    ) {
        public static TrackSearchItemResponse from(MusicSearchItemDto dto) {
            return new TrackSearchItemResponse(
                    dto.trackId(),
                    dto.songTitle(),
                    dto.artistName(),
                    dto.coverImg(),
                    dto.isrc()
            );
        }
    }
}

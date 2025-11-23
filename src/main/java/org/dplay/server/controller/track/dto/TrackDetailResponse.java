package org.dplay.server.controller.track.dto;

import org.dplay.server.domain.track.dto.TrackDetailResultDto;

public record TrackDetailResponse(
        String trackId,
        String songTitle,
        String artistName,
        String coverImg,
        String isrc
) {
    public static TrackDetailResponse from(TrackDetailResultDto dto) {
        return new TrackDetailResponse(
                dto.trackId(),
                dto.songTitle(),
                dto.artistName(),
                dto.coverImg(),
                dto.isrc()
        );
    }
}

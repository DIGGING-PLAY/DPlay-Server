package org.dplay.server.domain.track.dto;

public record TrackDetailResultDto(
        String trackId,
        String songTitle,
        String artistName,
        String coverImg,
        String isrc
) {
    public static TrackDetailResultDto of(
            String trackId,
            String songTitle,
            String artistName,
            String coverImg,
            String isrc
    ) {
        return new TrackDetailResultDto(
                trackId,
                songTitle,
                artistName,
                coverImg,
                isrc
        );
    }
}

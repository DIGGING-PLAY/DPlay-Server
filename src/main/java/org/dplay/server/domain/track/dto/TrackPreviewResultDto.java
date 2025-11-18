package org.dplay.server.domain.track.dto;

public record TrackPreviewResultDto(
        String sessionId,
        String trackId,
        String streamUrl
) {
    public static TrackPreviewResultDto of(
            String sessionId,
            String trackId,
            String streamUrl
    ) {
        return new TrackPreviewResultDto(
                sessionId,
                trackId,
                streamUrl
        );
    }
}

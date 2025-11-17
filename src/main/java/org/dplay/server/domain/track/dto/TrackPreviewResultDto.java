package org.dplay.server.domain.track.dto;

import java.time.Instant;

public record TrackPreviewResultDto(
        String sessionId,
        String trackId,
        String streamUrl,
        Instant expiresAt
) {
    public static TrackPreviewResultDto of(
            String sessionId,
            String trackId,
            String streamUrl,
            Instant expiresAt
    ) {
        return new TrackPreviewResultDto(
                sessionId,
                trackId,
                streamUrl,
                expiresAt
        );
    }
}

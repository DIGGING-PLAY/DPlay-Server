package org.dplay.server.controller.track.dto;

import org.dplay.server.domain.track.dto.TrackPreviewResultDto;

public record TrackPreviewResponse(
        String sessionId,
        String trackId,
        String streamUrl,
        String expiresAt
) {
    public static TrackPreviewResponse from(TrackPreviewResultDto dto) {
        return new TrackPreviewResponse(
                dto.sessionId(),
                dto.trackId(),
                dto.streamUrl(),
                dto.expiresAt() != null ? dto.expiresAt().toString() : null
        );
    }
}


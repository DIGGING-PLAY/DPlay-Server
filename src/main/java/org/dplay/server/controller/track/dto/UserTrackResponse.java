package org.dplay.server.controller.track.dto;

import org.dplay.server.domain.track.entity.Track;

public record UserTrackResponse(
        String trackId,
        String songTitle,
        String coverImg,
        String artistName
) {
    public static UserTrackResponse from(Track track) {
        return new UserTrackResponse(
                track.getTrackId(),
                track.getSongTitle(),
                track.getCoverImg(),
                track.getArtistName()
        );
    }
}

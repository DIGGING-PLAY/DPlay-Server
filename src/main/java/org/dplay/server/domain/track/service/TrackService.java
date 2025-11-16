package org.dplay.server.domain.track.service;

import org.dplay.server.domain.track.dto.TrackSearchResultDto;
import org.dplay.server.domain.track.entity.Track;

import java.util.Optional;

public interface TrackService {
    Optional<Track> findTrackByTrackId(String trackId);

    Track createTrackByPost(String trackId, String songTitle, String artistName, String coverImg, String isrc);

    Track createTrack(String trackId, String songTitle, String artistName, String coverImg, String isrc);

    TrackSearchResultDto searchTracks(String query, Integer limit, String storefront, String cursor);
}

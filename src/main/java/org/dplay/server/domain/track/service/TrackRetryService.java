package org.dplay.server.domain.track.service;

import org.dplay.server.domain.track.entity.Track;

import java.util.Optional;

public interface TrackRetryService {
    Optional<Track> findTrackByTrackIdWithRetry(String trackId);
}

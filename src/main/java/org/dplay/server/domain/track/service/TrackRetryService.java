package org.dplay.server.domain.track.service;

import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.global.exception.DPlayException;

public interface TrackRetryService {
    Track findTrackByTrackIdWithRetry(String trackId) throws DPlayException;
}

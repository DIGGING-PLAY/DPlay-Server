package org.dplay.server.domain.track.repository;

import org.dplay.server.domain.track.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findByTrackId(String trackId);
}



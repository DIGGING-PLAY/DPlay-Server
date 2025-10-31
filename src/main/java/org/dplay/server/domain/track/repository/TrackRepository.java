package org.dplay.server.domain.track.repository;

import org.dplay.server.domain.track.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {
}



package org.dplay.server.domain.track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.repository.TrackRepository;
import org.dplay.server.domain.track.service.TrackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;

    @Override
    public Optional<Track> findTrackByTrackId(String trackId) {
        return trackRepository.findByTrackId(trackId);
    }

    @Override
    @Transactional
    public Track createTrackByPost(String trackId, String songTitle, String artistName, String coverImg, String isrc) {
        return findTrackByTrackId(trackId)
                .orElseGet(() -> createTrack(trackId, songTitle, artistName, coverImg, isrc));
    }

    @Override
    @Transactional
    public Track createTrack(String trackId, String songTitle, String artistName, String coverImg, String isrc) {
        Track track = Track.builder()
                .trackId(trackId)
                .songTitle(songTitle)
                .artistName(artistName)
                .coverImg(coverImg)
                .isrc(isrc)
                .build();

        return trackRepository.save(track);
    }
}

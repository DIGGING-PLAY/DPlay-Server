package org.dplay.server.domain.track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.repository.TrackRepository;
import org.dplay.server.domain.track.service.TrackService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
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
        return findTrackByTrackIdWithRetry(trackId)
                .orElseGet(() -> {
                    try {
                        return createTrack(trackId, songTitle, artistName, coverImg, isrc);
                    } catch (DataIntegrityViolationException e) {
                        // 동시성 이슈: 다른 트랜잭션이 이미 생성한 경우
                        // 재조회를 재시도 (다른 트랜잭션의 커밋을 기다림)
                        log.warn("Track 생성 중 동시성 이슈 발생 (trackId: {}), 재조회 재시도", trackId);
                        return findTrackByTrackIdWithRetry(trackId)
                                .orElseThrow(() -> {
                                    log.error("Track 재조회 재시도 실패 (trackId: {})", trackId, e);
                                    return new DPlayException(ResponseError.CONCURRENT_OPERATION_FAILED);
                                });
                    }
                });
    }

    /**
     * Track 조회를 재시도하는 메서드
     * 동시성 이슈 발생 시 다른 트랜잭션의 커밋을 기다리기 위해 재시도
     * <p>
     * 참고: Optional을 반환하므로 @Recover는 사용하지 않음
     * 재시도 실패 시 자동으로 Optional.empty() 반환
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    private Optional<Track> findTrackByTrackIdWithRetry(String trackId) {
        return findTrackByTrackId(trackId);
    }

    @Override
    @Transactional
    public Track createTrack(String trackId, String songTitle, String artistName, String coverImg, String isrc) {
        log.debug("Track 생성 시도 (trackId: {})", trackId);
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

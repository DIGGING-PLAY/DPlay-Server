package org.dplay.server.domain.track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.repository.TrackRepository;
import org.dplay.server.domain.track.service.TrackRetryService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Track 조회 재시도 로직을 담당하는 서비스
 * Spring AOP 프록시를 통해 @Retryable이 정상 동작하도록 별도 빈으로 분리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TrackRetryServiceImpl implements TrackRetryService {

    private final TrackRepository trackRepository;

    /**
     * Track 조회를 재시도하는 메서드
     * 동시성 이슈 발생 시 다른 트랜잭션의 커밋을 기다리기 위해 재시도
     * <p>
     * 참고: Optional을 반환하므로 재시도 실패 시 자동으로 Optional.empty() 반환
     *
     * @param trackId 조회할 trackId
     * @return Optional<Track> 조회된 Track 또는 empty
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Optional<Track> findTrackByTrackIdWithRetry(String trackId) {
        log.debug("Track 조회 시도 (trackId: {})", trackId);
        return trackRepository.findByTrackId(trackId);
    }
}

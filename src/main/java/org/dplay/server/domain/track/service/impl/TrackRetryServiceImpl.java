package org.dplay.server.domain.track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.repository.TrackRepository;
import org.dplay.server.domain.track.service.TrackRetryService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Track 조회 재시도 로직을 담당하는 서비스
 * 별도 빈으로 분리하여 프록시를 통한 호출을 보장하고, 수동 재시도 로직으로 구현
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TrackRetryServiceImpl implements TrackRetryService {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_DELAY_MS = 100;

    private final TrackRepository trackRepository;

    /**
     * Track 조회를 재시도하는 메서드
     * 동시성 이슈 발생 시 다른 트랜잭션의 커밋을 기다리기 위해 재시도
     * <p>
     * Track을 찾지 못한 경우 DPlayException(TARGET_NOT_FOUND)을 던져 수동 재시도 로직이 동작하도록 함
     * Optional.empty()를 반환하면 재시도가 발생하지 않으므로 예외를 던지는 방식 사용
     * <p>
     * 참고: DPlayException을 @Retryable에 지정하면 다른 DPlayException도 재시도될 수 있어
     * 수동 재시도 로직으로 구현함
     *
     * @param trackId 조회할 trackId
     * @return Track 조회된 Track
     * @throws DPlayException Track을 찾을 수 없을 때 TARGET_NOT_FOUND (재시도 대상)
     */
    @Override
    public Track findTrackByTrackIdWithRetry(String trackId) throws DPlayException {
        long delayMs = INITIAL_DELAY_MS;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            log.debug("Track 조회 시도 (attempt: {}/{}, trackId: {})", attempt, MAX_RETRY_ATTEMPTS, trackId);

            Track track = trackRepository.findByTrackId(trackId)
                    .orElse(null);

            if (track != null) {
                if (attempt > 1) {
                    log.info("Track 조회 성공 (attempt: {}, trackId: {})", attempt, trackId);
                }
                return track;
            }

            // 마지막 시도가 아니면 대기 후 재시도
            if (attempt < MAX_RETRY_ATTEMPTS) {
                try {
                    log.debug("Track 조회 실패 (attempt: {}/{}, trackId: {}), {}ms 후 재시도",
                            attempt, MAX_RETRY_ATTEMPTS, trackId, delayMs);
                    Thread.sleep(delayMs);
                    delayMs *= 2; // 지수 백오프
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Track 조회 재시도 중 인터럽트 발생 (trackId: {})", trackId);
                    break;
                }
            }
        }

        // 모든 재시도 실패
        throw new DPlayException(ResponseError.TARGET_NOT_FOUND);
    }
}

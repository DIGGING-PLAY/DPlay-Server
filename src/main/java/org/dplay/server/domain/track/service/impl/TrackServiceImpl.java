package org.dplay.server.domain.track.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dplay.server.domain.music.openfeign.apple.service.AppleMusicService;
import org.dplay.server.domain.track.dto.TrackSearchResultDto;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.track.repository.TrackRepository;
import org.dplay.server.domain.track.service.TrackService;
import org.dplay.server.global.exception.DPlayException;
import org.dplay.server.global.response.ResponseError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final AppleMusicService appleMusicService;
    private static final String DEFAULT_STOREFRONT = "kr";
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;

    @Override
    public Optional<Track> findTrackByTrackId(String trackId) {
        return trackRepository.findByTrackId(trackId);
    }

    @Override
    @Transactional
    public Track createTrackByPost(String trackId, String songTitle, String artistName, String coverImg, String isrc) {
        Track foundTrack = tryFindTrack(trackId);
        if (foundTrack != null) {
            return foundTrack;
        }

        return createTrackSafely(trackId, songTitle, artistName, coverImg, isrc);
    }

    /**
     * Track을 조회 시도하는 메서드
     * Track을 찾지 못하면 null을 반환
     *
     * @param trackId 조회할 trackId
     * @return 조회된 Track, 없으면 null
     */
    private Track tryFindTrack(String trackId) {
        return trackRepository.findByTrackId(trackId).orElse(null);
    }

    /**
     * Track을 안전하게 생성하는 메서드
     * 동시성 이슈를 처리하여 다른 트랜잭션이 이미 생성한 경우 재조회를 시도
     *
     * @param trackId    트랙 ID
     * @param songTitle  노래 제목
     * @param artistName 아티스트 이름
     * @param coverImg   앨범 커버 이미지 URL
     * @param isrc       ISRC 코드
     * @return 생성되거나 조회된 Track
     * @throws DPlayException 동시성 처리 후에도 Track을 찾을 수 없는 경우 CONCURRENT_OPERATION_FAILED
     */
    private Track createTrackSafely(String trackId, String songTitle, String artistName, String coverImg, String isrc) {
        try {
            return createTrack(trackId, songTitle, artistName, coverImg, isrc);
        } catch (DataIntegrityViolationException e) {
            // 동시성 이슈: 다른 트랜잭션이 이미 생성한 경우
            log.warn("Track 생성 중 동시성 이슈 발생 (trackId: {}), 재조회 시도", trackId);
            return handleConcurrencyConflict(trackId);
        }
    }

    /**
     * 동시성 충돌 발생 시 재조회를 시도하는 메서드
     *
     * @param trackId 조회할 trackId
     * @return 재조회된 Track
     * @throws DPlayException 재조회 후에도 Track을 찾을 수 없는 경우 CONCURRENT_OPERATION_FAILED
     */
    private Track handleConcurrencyConflict(String trackId) {
        Track retriedTrack = tryFindTrack(trackId);
        if (retriedTrack != null) {
            return retriedTrack;
        }

        // 재시도 후에도 찾지 못한 경우 → 예상치 못한 상황
        log.error("Track 재조회 재시도 실패 (trackId: {})", trackId);
        throw new DPlayException(ResponseError.CONCURRENT_OPERATION_FAILED);
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

    @Override
    public TrackSearchResultDto searchTracks(String query, Integer limit, String storefront, String cursor) {
        // 파라미터 검증 및 기본값 설정
        String finalStorefront = storefront != null && !storefront.isEmpty() ? storefront : DEFAULT_STOREFRONT;
        int finalLimit = limit != null ? Math.min(Math.max(limit, 1), MAX_LIMIT) : DEFAULT_LIMIT;

        // 커서에서 offset 추출
        Integer offset = parseOffsetFromCursor(cursor);

        // Apple Music API 호출
        AppleMusicService.MusicSearchResult result = appleMusicService.searchMusic(
                query,
                finalLimit,
                finalStorefront,
                offset
        );

        return TrackSearchResultDto.of(
                query,
                finalStorefront,
                finalLimit,
                result.nextCursor(),
                result.items()
        );
    }

    private Integer parseOffsetFromCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return Integer.parseInt(decoded);
        } catch (Exception e) {
            return null;
        }
    }
}

package org.dplay.server.domain.music.openfeign.apple;

import org.dplay.server.domain.music.openfeign.apple.dto.AppleMusicSearchResponse;
import org.dplay.server.domain.music.openfeign.apple.dto.AppleMusicTrackDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "apple-music-client", url = "${apple.music.base-url}")
public interface AppleMusicFeignClient {

    @GetMapping("/v1/catalog/{storefront}/search")
    AppleMusicSearchResponse search(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("term") String term,
            @RequestParam(value = "types", defaultValue = "songs") String types,
            @RequestParam(value = "limit", defaultValue = "20") Integer limit,
            @RequestParam(value = "offset", required = false) Integer offset,
            @PathVariable("storefront") String storefront
    );

    @GetMapping("/v1/catalog/{storefront}/songs/{id}")
    AppleMusicTrackDetailResponse getTrackDetail(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("storefront") String storefront,
            @PathVariable("id") String id
    );
}


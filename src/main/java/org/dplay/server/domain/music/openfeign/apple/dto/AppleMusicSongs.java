package org.dplay.server.domain.music.openfeign.apple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleMusicSongs(
        @JsonProperty("data")
        List<AppleMusicTrackData> data,
        @JsonProperty("next")
        String next
) {
}

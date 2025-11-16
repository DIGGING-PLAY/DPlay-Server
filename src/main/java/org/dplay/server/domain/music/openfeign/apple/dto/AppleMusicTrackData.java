package org.dplay.server.domain.music.openfeign.apple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleMusicTrackData(
        @JsonProperty("id")
        String id,
        @JsonProperty("type")
        String type,
        @JsonProperty("attributes")
        AppleMusicTrackAttributes attributes
) {
}

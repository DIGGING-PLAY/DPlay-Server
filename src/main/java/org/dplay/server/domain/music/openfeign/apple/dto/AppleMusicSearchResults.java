package org.dplay.server.domain.music.openfeign.apple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleMusicSearchResults(
        @JsonProperty("songs")
        AppleMusicSongs songs
) {
}

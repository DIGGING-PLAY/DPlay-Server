package org.dplay.server.domain.music.openfeign.apple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleMusicTrackAttributes(
        @JsonProperty("name")
        String name,
        @JsonProperty("artistName")
        String artistName,
        @JsonProperty("albumName")
        String albumName,
        @JsonProperty("artwork")
        AppleMusicArtwork artwork,
        @JsonProperty("durationInMillis")
        Long durationInMillis,
        @JsonProperty("isrc")
        String isrc,
        @JsonProperty("previews")
        List<AppleMusicPreview> previews
) {
}

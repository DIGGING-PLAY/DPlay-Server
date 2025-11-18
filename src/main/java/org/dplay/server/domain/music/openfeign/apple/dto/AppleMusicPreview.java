package org.dplay.server.domain.music.openfeign.apple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleMusicPreview(
        @JsonProperty("url")
        String url
) {
}

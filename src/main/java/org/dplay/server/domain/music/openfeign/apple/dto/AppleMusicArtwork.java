package org.dplay.server.domain.music.openfeign.apple.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AppleMusicArtwork(
        @JsonProperty("url")
        String url,
        @JsonProperty("width")
        Integer width,
        @JsonProperty("height")
        Integer height
) {
    public String getCoverImageUrl() {
        if (url == null) {
            return null;
        }
        return url.replace("{w}", "512").replace("{h}", "512");
    }
}

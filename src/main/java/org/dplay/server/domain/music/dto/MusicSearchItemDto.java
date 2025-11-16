package org.dplay.server.domain.music.dto;

import lombok.Builder;

@Builder
public record MusicSearchItemDto(
        String trackId,
        String songTitle,
        String artistName,
        String coverImg,
        String isrc,
        String previewUrl
) {
}

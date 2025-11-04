package org.dplay.server.controller.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PostRequest(
        @NotBlank(message = "트랙 ID는 필수입니다.")
        String trackId,

        @NotBlank(message = "노래 제목은 필수입니다.")
        String songTitle,

        @NotBlank(message = "아티스트 이름은 필수입니다.")
        String artistName,

        @NotBlank(message = "앨범 커버 이미지 URL은 필수입니다.")
        String coverImg,

        @NotBlank(message = "ISRC는 필수입니다.")
        String isrc,

        @NotBlank(message = "추천 이유는 필수입니다.")
        @Size(min = 1, max = 100, message = "추천 이유는 1자 이상 100자 이하여야 합니다.")
        String content
) {
}

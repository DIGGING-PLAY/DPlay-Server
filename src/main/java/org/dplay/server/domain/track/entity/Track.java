package org.dplay.server.domain.track.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dplay.server.domain.common.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "track",
        indexes = {
                @Index(name = "idx_track_artist", columnList = "artist_name")
        }
)
public class Track extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trackId;

    @Column(nullable = false)
    private String songTitle;

    @Column(nullable = false)
    private String artistName;

    private String albumName;

    private String coverImg;

    private Long durationMs;

    private String isrc;

    private String previewUrl;

    @Builder
    private Track(
            String songTitle,
            String artistName,
            String albumName,
            String coverImg,
            Long durationMs,
            String isrc,
            String previewUrl
    ) {
        this.songTitle = songTitle;
        this.artistName = artistName;
        this.albumName = albumName;
        this.coverImg = coverImg;
        this.durationMs = durationMs;
        this.isrc = isrc;
        this.previewUrl = previewUrl;
    }
}



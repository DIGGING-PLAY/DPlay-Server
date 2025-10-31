package org.dplay.server.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dplay.server.domain.common.BaseTimeEntity;
import org.dplay.server.domain.user.Platform;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_nickname", columnNames = {"nickname"}),
                @UniqueConstraint(name = "uk_users_platform_id", columnNames = {"platform_id"}),
                @UniqueConstraint(name = "uk_users_email", columnNames = {"email"})
        }
)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false)
    private String nickname;

    private String profileImg;

    @Column(nullable = false)
    private String platformId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(nullable = false)
    private String email;

    @Lob
    private String refreshToken;

    @Column(nullable = false)
    private boolean pushOptIn = false;

    @Builder
    private User(
            String nickname,
            String profileImg,
            String platformId,
            Platform platform,
            String email,
            String refreshToken,
            boolean pushOptIn
    ) {
        this.nickname = nickname;
        this.profileImg = profileImg;
        this.platformId = platformId;
        this.platform = platform;
        this.email = email;
        this.refreshToken = refreshToken;
        this.pushOptIn = pushOptIn;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean validateRefreshToken(String refreshToken) {
        return this.refreshToken.equals(refreshToken);
    }
}



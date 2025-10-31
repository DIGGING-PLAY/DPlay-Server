package org.dplay.server.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dplay.server.domain.common.BaseTimeEntity;
import org.dplay.server.domain.user.entity.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post_save",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_save_post_user", columnNames = {"post_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_post_save_post", columnList = "post_id"),
                @Index(name = "idx_post_save_user", columnList = "user_id")
        }
)
public class PostScrap extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scrapId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Builder
    private PostScrap(Post post, User user) {
        this.post = post;
        this.user = user;
    }
}

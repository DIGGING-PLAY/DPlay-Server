package org.dplay.server.domain.post.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dplay.server.domain.common.BaseTimeEntity;
import org.dplay.server.domain.question.entity.Question;
import org.dplay.server.domain.track.entity.Track;
import org.dplay.server.domain.user.entity.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "post",
        indexes = {
                @Index(name = "idx_post_user", columnList = "user_id"),
                @Index(name = "idx_post_track", columnList = "track_id"),
                @Index(name = "idx_post_user_question", columnList = "user_id,question_id")
        }
)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(optional = false)
    @JoinColumn(name = "track_id", nullable = false)
    private Track track;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int saveCount = 0;

    @Builder
    private Post(
            User user,
            Question question,
            Track track,
            String content,
            int likeCount,
            int saveCount
    ) {
        this.user = user;
        this.question = question;
        this.track = track;
        this.content = content;
        this.likeCount = likeCount;
        this.saveCount = saveCount;
    }

    /**
     * 좋아요 개수를 증가시킵니다.
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * 좋아요 개수를 감소시킵니다.
     * likeCount가 0보다 작아지지 않도록 보장합니다.
     */
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * 스크랩 개수를 증가시킵니다.
     */
    public void incrementSaveCount() {
        this.saveCount++;
    }

    /**
     * 스크랩 개수를 감소시킵니다.
     * saveCount가 0보다 작아지지 않도록 보장합니다.
     */
    public void decrementSaveCount() {
        if (this.saveCount > 0) {
            this.saveCount--;
        }
    }
}

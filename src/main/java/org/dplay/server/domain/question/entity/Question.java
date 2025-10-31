package org.dplay.server.domain.question.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.dplay.server.domain.common.BaseTimeEntity;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "question",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_question_date", columnNames = {"display_date"})
        },
        indexes = {
                @Index(name = "uk_question_date", columnList = "display_date", unique = true)
        }
)
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long questionId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate displayDate;

    @Column(nullable = false)
    private int postCount = 0;

    @Builder
    private Question(String title, LocalDate displayDate, int postCount) {
        this.title = title;
        this.displayDate = displayDate;
        this.postCount = postCount;
    }
}

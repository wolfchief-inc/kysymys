package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "reviewComment")
@Table(name = "review_comments")
@Data
public class ReviewCommentJpaEntity {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private AnswerJpaEntity answer;

    @Column(name = "commenter_id", nullable = false)
    private String commenterId;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "posted_at", nullable = false)
    private LocalDateTime postedAt;
}

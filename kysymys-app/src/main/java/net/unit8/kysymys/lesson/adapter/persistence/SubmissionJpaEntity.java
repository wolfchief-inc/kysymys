package net.unit8.kysymys.lesson.adapter.persistence;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.time.LocalDateTime;

public class SubmissionJpaEntity {
    @ManyToOne
    private AnswerJpaEntity answer;

    @Column(name = "commit_hash")
    private String commitHash;

    @Column(name = "submitted_at")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime submittedAt;
}

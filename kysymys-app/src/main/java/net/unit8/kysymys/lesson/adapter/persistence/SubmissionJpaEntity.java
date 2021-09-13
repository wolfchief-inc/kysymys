package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity(name = "submission")
@Table(name = "submissions")
@Data
public class SubmissionJpaEntity {
    @Id
    private String id;

    @ManyToOne
    private AnswerJpaEntity answer;

    @Column(name = "commit_hash")
    private String commitHash;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Override
    public String toString() {
        return "SubmissionEntity{" +
                "id='" + id + '\'' +
                ", answer=" + answer.getId() +
                ", commitHash='" + commitHash + '\'' +
                ", submittedAt=" + submittedAt +
                '}';
    }
}

package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "latestSubmission")
@Table(name = "latest_submission")
@Data
public class LatestSubmissionJpaEntity implements Serializable {
    @Id
    @ManyToOne
    @JoinColumn(name = "answer_id")
    private AnswerJpaEntity answer;

    @ManyToOne
    private SubmissionJpaEntity submission;
}

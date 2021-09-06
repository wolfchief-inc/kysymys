package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity(name = "latestSubmission")
@Table(name = "latest_submission")
@Data
public class LatestSubmissionJpaEntity {
    @ManyToOne
    private AnswerJpaEntity answer;

    @ManyToOne
    private SubmissionJpaEntity submission;
}

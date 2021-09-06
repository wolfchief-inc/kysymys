package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;

import javax.persistence.*;

@Entity(name = "answer")
@Table(name = "answers")
@Data
public class AnswerJpaEntity {
    @Id
    private String id;

    @Column(name = "problem_id", nullable = false)
    private String problemId;

    @Column(name = "answerer_id", nullable = false)
    private String answererId;

    @Column(name = "repository_url")
    private String repositoryUrl;

    @OneToOne
    private SubmissionJpaEntity latestSubmission;
}

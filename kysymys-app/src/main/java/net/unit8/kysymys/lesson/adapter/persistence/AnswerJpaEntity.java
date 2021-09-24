package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;

import javax.persistence.*;

@Entity(name = "answer")
@Table(name = "answers")
@Data
public class AnswerJpaEntity {
    @Id
    @Column(name = "id", length = 21)
    private String id;

    @ManyToOne(cascade = CascadeType.REMOVE)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemJpaEntity problem;

    @Column(name = "answerer_id", nullable = false)
    private String answererId;

    @Column(name = "repository_url")
    private String repositoryUrl;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "latest_submissions",
            joinColumns =
                    { @JoinColumn(name = "answer_id", referencedColumnName = "id")},
            inverseJoinColumns =
                    { @JoinColumn(name = "submission_id", referencedColumnName = "id")})
    private SubmissionJpaEntity latestSubmission;
}

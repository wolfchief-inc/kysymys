package net.unit8.kysymys.lesson.adapter.persistence;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

public class ReviewCommentJpaEntity {
    @Id
    private String id;

    @ManyToOne
    private AnswerJpaEntity answer;

    @Column(name = "commenter_id")
    private String commenterId;

    private String description;
}

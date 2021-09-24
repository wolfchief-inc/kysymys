package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "problemCreated")
@Table(name = "problem_created_events")
@DiscriminatorValue("problem_created")
public class ProblemCreatedJpaEntity extends ProblemEventJpaEntity {
    @Getter
    @Setter
    @Column(name="creator_id", nullable = false)
    private String creatorId;
}

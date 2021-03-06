package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "problemEvent")
@Table(name = "problem_events")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ProblemEventJpaEntity implements Serializable {
    @Id
    @Getter
    @Setter
    @EqualsAndHashCode.Include
    private String id;

    @Getter
    @Setter
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "problem_lifecycle_id")
    private ProblemLifecycleJpaEntity problemLifecycle;

    @Getter
    @Setter
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}

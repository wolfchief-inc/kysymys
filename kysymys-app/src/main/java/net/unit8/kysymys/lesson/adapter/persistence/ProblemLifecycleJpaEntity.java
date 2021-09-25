package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.unit8.kysymys.lesson.domain.ProblemStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity(name= "problemLifecycle")
@Table(name = "problem_lifecycles")
public class ProblemLifecycleJpaEntity implements Serializable {
    @Id
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    @OneToOne(optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemJpaEntity problem;

    @Getter
    @Setter
    @Column(nullable = false)
    @Enumerated
    private ProblemStatus status;

    @Getter
    @Setter
    @OneToMany(mappedBy = "problemLifecycle", cascade = CascadeType.ALL)
    private List<ProblemEventJpaEntity> problemEvents;

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object another) {
        return Optional.ofNullable(another)
                .filter(ProblemLifecycleJpaEntity.class::isInstance)
                .map(ProblemLifecycleJpaEntity.class::cast)
                .filter(o -> Objects.equals(o.id, id))
                .isPresent();
    }
}

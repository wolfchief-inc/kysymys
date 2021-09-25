package net.unit8.kysymys.lesson.adapter.persistence;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;
import java.util.Optional;

@Entity(name = "problem")
@Table(name ="problems")
public class ProblemJpaEntity {
    @Getter
    @Setter
    @Id
    @Column(name = "id", length = 21)
    private String id;

    @Getter
    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(name = "repository_url", nullable = false)
    private String repositoryUrl;

    @Getter
    @Setter
    @Column(name = "branch", nullable = false)
    private String branch;

    @Getter
    @Setter
    @Column(name = "readme_path")
    private String readmePath;

    @Getter
    @Setter
    @Column(name = "runner")
    private String runner;

    @Getter
    @Setter
    @OneToOne(mappedBy = "problem")
    private ProblemLifecycleJpaEntity problemLifecycle;

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object another) {
        return Optional.ofNullable(another)
                .filter(ProblemJpaEntity.class::isInstance)
                .map(ProblemJpaEntity.class::cast)
                .filter(o -> Objects.equals(o.id, id))
                .isPresent();
    }

}

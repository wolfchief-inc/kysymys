package net.unit8.kysymys.lesson.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProblemLifecycleRepository extends JpaRepository<ProblemLifecycleJpaEntity, String> {
    Optional<ProblemLifecycleJpaEntity> findByProblemId(String problemId);
}

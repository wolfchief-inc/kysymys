package net.unit8.kysymys.lesson.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemLifecycleRepository extends JpaRepository<ProblemLifecycleJpaEntity, String> {
    ProblemLifecycleJpaEntity findByProblemId(String problemId);
}

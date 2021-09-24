package net.unit8.kysymys.lesson.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemEventRepository extends JpaRepository<ProblemEventJpaEntity, String> {
}

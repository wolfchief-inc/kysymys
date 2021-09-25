package net.unit8.kysymys.lesson.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProblemRepository extends JpaRepository<ProblemJpaEntity, String> {
    @Query("SELECT p FROM problem p JOIN p.problemLifecycle pl JOIN pl.problemEvents pe WHERE TYPE(pe) = problemCreated ORDER BY pl.status, pe.occurredAt DESC")
    Page<ProblemJpaEntity> findAllOrderedByCreatedAt(Pageable pageable);
}

package net.unit8.kysymys.lesson.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<SubmissionJpaEntity, String> {
    @Query("SELECT s FROM submission s WHERE commit_hash = :commitHash AND answer_id = :answerId")
    Optional<SubmissionJpaEntity> findByCommitHash(@Param("answerId") String answerId,
                                                   @Param("commitHash") String commitHash);
}

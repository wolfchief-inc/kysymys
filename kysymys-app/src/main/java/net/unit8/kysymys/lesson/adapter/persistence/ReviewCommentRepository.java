package net.unit8.kysymys.lesson.adapter.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewCommentJpaEntity, String> {
    @Query("SELECT rc FROM reviewComment rc "
            + "WHERE rc.answer.id=?1 AND rc.id < ?2")
    List<ReviewCommentJpaEntity> findByCursor(String answerId, String cursor, Pageable page);
}

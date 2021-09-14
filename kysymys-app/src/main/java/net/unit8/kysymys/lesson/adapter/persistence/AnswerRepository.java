package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.domain.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<AnswerJpaEntity, String> {
    @Query("SELECT a FROM answer a JOIN a.problem WHERE a.answererId = ?1 ORDER BY a.latestSubmission.submittedAt DESC")
    Page<AnswerJpaEntity> findRecentAnswers(String value, PageRequest of);
}

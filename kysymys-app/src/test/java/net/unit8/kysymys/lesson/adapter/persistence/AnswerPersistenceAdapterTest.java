package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.domain.AnswerRepository;
import net.unit8.kysymys.lesson.domain.ProblemRepository;
import net.unit8.kysymys.lesson.domain.*;
import net.unit8.kysymys.user.domain.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({AnswerPersistenceAdapter.class, ProblemPersistenceAdapter.class, AnswerMapper.class, ProblemMapper.class})
class AnswerPersistenceAdapterTest {
    @Autowired
    private AnswerPersistenceAdapter sut;

    @Autowired
    private ProblemPersistenceAdapter problemPersistenceAdapter;

    @Autowired
    private net.unit8.kysymys.lesson.adapter.persistence.AnswerRepository answerRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Test
    void test() {
        Problem problem = Problem.of(new ProblemId(),
                ProblemName.of("problem1"),
                ProblemRepository.of("https://github.com/kawasima/problem1", "main"));
        problemPersistenceAdapter.save(problem);

        AnswerId answerId = new AnswerId();
        Answer answer1st = Answer.of(answerId,
                problem,
                new UserId(),
                AnswerRepository.of(
                        "https://github.com/kawasima/answer1",
                        "0123456789012345678901234567890123456789"
                ),
                LocalDateTime.now());
        sut.save(answer1st);
        answerRepository.flush();

        Answer answer2nd = Answer.of(answerId,
                problem,
                new UserId(),
                AnswerRepository.of(
                        "https://github.com/kawasima/answer1",
                        "1123456789012345678901234567890123456789"
                ),
                LocalDateTime.now());
        sut.save(answer2nd);
        answerRepository.flush();

        assertThat(answerRepository.findAll()).hasSize(1);
        assertThat(submissionRepository.findAll()).hasSize(2);
    }
}
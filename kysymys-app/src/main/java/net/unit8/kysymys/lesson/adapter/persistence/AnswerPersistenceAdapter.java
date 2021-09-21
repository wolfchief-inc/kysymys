package net.unit8.kysymys.lesson.adapter.persistence;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.lesson.application.CountAnswersPort;
import net.unit8.kysymys.lesson.application.ListRecentAnswersPort;
import net.unit8.kysymys.lesson.application.LoadAnswerPort;
import net.unit8.kysymys.lesson.application.SaveAnswerPort;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.AnswerId;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.stereotype.PersistenceAdapter;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;

@PersistenceAdapter
class AnswerPersistenceAdapter implements LoadAnswerPort, SaveAnswerPort, ListRecentAnswersPort, CountAnswersPort {
    private final AnswerRepository answerRepository;
    private final SubmissionRepository submissionRepository;
    private final AnswerMapper answerMapper;

    AnswerPersistenceAdapter(AnswerRepository answerRepository, SubmissionRepository submissionRepository, AnswerMapper answerMapper) {
        this.answerRepository = answerRepository;
        this.submissionRepository = submissionRepository;
        this.answerMapper = answerMapper;
    }

    @Override
    public Optional<Answer> load(AnswerId answerId) {
        return answerRepository.findById(answerId.getValue())
                .map(answerMapper::entityToDomain);
    }

    public Optional<Answer> load(ProblemId problemId, UserId answererId) {
        AnswerJpaEntity example = new AnswerJpaEntity();
        ProblemJpaEntity problemExample = new ProblemJpaEntity();
        problemExample.setId(problemId.getValue());
        example.setProblem(problemExample);
        example.setAnswererId(answererId.getValue());
        return answerRepository.findOne(Example.of(example))
                .map(answerMapper::entityToDomain);
    }

    @Override
    public Page<Answer> listRecentAnswers(UserId answererId, int page, int size) {
        if (page > 0) page = page - 1;
        return answerRepository.findRecentAnswers(answererId.getValue(), PageRequest.of(page, size))
                .map(answerMapper::entityToDomain);
    }

    @Override
    public void save(Answer answer) {
        Optional<SubmissionJpaEntity> submission = submissionRepository.findByCommitHash(answer.getId().getValue(),
                answer.getRepository().getCommitHash());
        if (submission.isPresent()) return;

        AnswerJpaEntity answerEntity = answerRepository.findById(answer.getId().getValue())
                .orElse(answerMapper.domainToEntity(answer));

        SubmissionJpaEntity submissionEntity = new SubmissionJpaEntity();
        submissionEntity.setId(NanoIdUtils.randomNanoId());
        submissionEntity.setAnswer(answerEntity);
        submissionEntity.setCommitHash(answer.getRepository().getCommitHash());
        submissionEntity.setSubmittedAt(LocalDateTime.now());

        answerEntity.setLatestSubmission(submissionEntity);
        answerRepository.save(answerEntity);
    }

    @Override
    public long countByProblemId(ProblemId problemId) {
        AnswerJpaEntity example = new AnswerJpaEntity();
        ProblemJpaEntity problemExample = new ProblemJpaEntity();
        problemExample.setId(problemId.getValue());
        example.setProblem(problemExample);
        return answerRepository.count(Example.of(example));
    }
}

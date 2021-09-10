package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.application.LoadAnswerPort;
import net.unit8.kysymys.lesson.application.SaveAnswerPort;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.AnswerId;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class AnswerPersistenceAdapter implements LoadAnswerPort, SaveAnswerPort {
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
    public void save(Answer answer) {
        Optional<SubmissionJpaEntity> submission = submissionRepository.findByCommitHash(answer.getId().getValue(),
                answer.getRepository().getCommitHash());
        if (submission.isPresent()) return;

        AnswerJpaEntity entity = answerMapper.domainToEntity(answer);
        answerRepository.save(entity);
    }
}

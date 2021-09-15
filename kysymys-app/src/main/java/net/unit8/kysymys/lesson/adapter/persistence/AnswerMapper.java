package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.AnswerId;
import net.unit8.kysymys.lesson.domain.AnswerRepository;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;

@Component
class AnswerMapper {
    private final ProblemMapper problemMapper;

    AnswerMapper(ProblemMapper problemMapper) {
        this.problemMapper = problemMapper;
    }

    AnswerJpaEntity domainToEntity(Answer answer) {
        AnswerJpaEntity entity = new AnswerJpaEntity();
        entity.setId(answer.getId().getValue());
        entity.setAnswererId(answer.getAnswererId().getValue());
        entity.setProblem(problemMapper.domainToEntity(answer.getProblem()));
        entity.setRepositoryUrl(answer.getRepository().getUrl());
        return entity;
    }

    Answer entityToDomain(AnswerJpaEntity entity) {
        return Answer.of(
                AnswerId.of(entity.getId()),
                problemMapper.entityToDomain(entity.getProblem()),
                UserId.of(entity.getAnswererId()),
                AnswerRepository.of(entity.getRepositoryUrl(), entity.getLatestSubmission().getCommitHash()),
                entity.getLatestSubmission().getSubmittedAt()
        );
    }
}

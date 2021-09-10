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

        return entity;
    }

    Answer entityToDomain(AnswerJpaEntity entity) {
        return Answer.of(
                AnswerId.of(entity.getId()),
                problemMapper.entityToDomain(entity.getProblem()),
                UserId.of(entity.getAnswererId()),
                AnswerRepository.of(entity.getRepositoryUrl(), entity.getLatestSubmission().getCommitHash())
        );
    }
}

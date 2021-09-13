package net.unit8.kysymys.lesson.adapter.persistence;

import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.lesson.domain.ProblemName;
import net.unit8.kysymys.lesson.domain.ProblemRepository;
import org.springframework.stereotype.Component;

@Component
class ProblemMapper {
    public ProblemJpaEntity domainToEntity(Problem problem) {
        ProblemJpaEntity entity = new ProblemJpaEntity();
        entity.setId(problem.getId().getValue());
        entity.setName(problem.getName().getValue());
        entity.setRepositoryUrl(problem.getRepository().getUrl());
        entity.setBranch(problem.getRepository().getBranch());
        return entity;
    }

    public Problem entityToDomain(ProblemJpaEntity entity) {

        return Problem.validator.validated(
                ProblemId.of(entity.getId()),
                ProblemName.of(entity.getName()),
                ProblemRepository.of(entity.getRepositoryUrl(),
                        entity.getBranch())
        );
    }
}

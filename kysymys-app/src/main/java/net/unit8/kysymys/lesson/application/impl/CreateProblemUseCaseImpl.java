package net.unit8.kysymys.lesson.application.impl;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.lesson.application.CreateProblemCommand;
import net.unit8.kysymys.lesson.application.CreateProblemUseCase;
import net.unit8.kysymys.lesson.application.SaveProblemPort;
import net.unit8.kysymys.lesson.domain.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class CreateProblemUseCaseImpl implements CreateProblemUseCase {
    private final SaveProblemPort saveProblemPort;
    private final TransactionTemplate tx;

    public CreateProblemUseCaseImpl(SaveProblemPort saveProblemPort, TransactionTemplate tx) {
        this.saveProblemPort = saveProblemPort;
        this.tx = tx;
    }

    @Override
    public CreatedProblemEvent handle(CreateProblemCommand command) {
        ProblemRepository problemRepository = ProblemRepository.validator.validate(command.getRepositoryUrl(), command.getBranch(), command.getReadmePath())
                .orElseThrow(ConstraintViolationsException::new);
        ProblemName problemName = ProblemName.validator.validate(command.getName())
                .orElseThrow(ConstraintViolationsException::new);

        Problem problem = Problem.validator.validate(
                new ProblemId(),
                problemName,
                problemRepository
        ).orElseThrow(ConstraintViolationsException::new);

        return tx.execute(status -> {
            saveProblemPort.save(problem);
            return new CreatedProblemEvent(problem.getId());
        });
    }
}

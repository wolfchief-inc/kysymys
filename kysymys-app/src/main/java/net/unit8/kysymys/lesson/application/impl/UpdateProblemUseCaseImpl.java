package net.unit8.kysymys.lesson.application.impl;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.lesson.application.SaveProblemPort;
import net.unit8.kysymys.lesson.application.UpdateProblemCommand;
import net.unit8.kysymys.lesson.application.UpdateProblemUseCase;
import net.unit8.kysymys.lesson.application.UpdatedProblemEvent;
import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.lesson.domain.ProblemName;
import net.unit8.kysymys.lesson.domain.ProblemRepository;
import net.unit8.kysymys.steleotype.UseCase;
import org.springframework.transaction.support.TransactionTemplate;

@UseCase
public class UpdateProblemUseCaseImpl implements UpdateProblemUseCase {
    private final SaveProblemPort saveProblemPort;
    private final TransactionTemplate tx;

    public UpdateProblemUseCaseImpl(SaveProblemPort saveProblemPort, TransactionTemplate tx) {
        this.saveProblemPort = saveProblemPort;
        this.tx = tx;
    }

    @Override
    public UpdatedProblemEvent handle(UpdateProblemCommand command) {
        ProblemId problemId = ProblemId.of(command.getId());
        ProblemRepository problemRepository = ProblemRepository.validator.validate(command.getRepositoryUrl(), command.getBranch(), command.getReadmePath())
                .orElseThrow(ConstraintViolationsException::new);
        ProblemName problemName = ProblemName.validator.validate(command.getName())
                .orElseThrow(ConstraintViolationsException::new);

        Problem problem = Problem.validator.validate(
                problemId,
                problemName,
                problemRepository
        ).orElseThrow(ConstraintViolationsException::new);

        return tx.execute(status -> {
            saveProblemPort.save(problem);
            return new UpdatedProblemEvent(problemId);
        });
    }
}

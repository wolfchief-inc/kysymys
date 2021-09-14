package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.DeleteProblemCommand;
import net.unit8.kysymys.lesson.application.DeleteProblemPort;
import net.unit8.kysymys.lesson.application.DeleteProblemUseCase;
import net.unit8.kysymys.lesson.application.DeletedProblemEvent;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.steleotype.UseCase;
import org.springframework.transaction.support.TransactionTemplate;

@UseCase
class DeleteProblemUseCaseImpl implements DeleteProblemUseCase {
    private final DeleteProblemPort deleteProblemPort;
    private final TransactionTemplate tx;

    DeleteProblemUseCaseImpl(DeleteProblemPort deleteProblemPort, TransactionTemplate tx) {
        this.deleteProblemPort = deleteProblemPort;
        this.tx = tx;
    }

    @Override
    public DeletedProblemEvent handle(DeleteProblemCommand command) {
        ProblemId problemId = ProblemId.of(command.getId());
        return tx.execute(status -> {
            deleteProblemPort.delete(problemId);
            return new DeletedProblemEvent(problemId);
        });
    }
}

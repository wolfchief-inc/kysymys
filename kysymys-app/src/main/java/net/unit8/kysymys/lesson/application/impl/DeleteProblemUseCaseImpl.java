package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.stereotype.UseCase;
import org.springframework.transaction.support.TransactionTemplate;

@UseCase
class DeleteProblemUseCaseImpl implements DeleteProblemUseCase {
    private final DeleteProblemPort deleteProblemPort;
    private final CountAnswersPort countAnswersPort;
    private final TransactionTemplate tx;

    DeleteProblemUseCaseImpl(DeleteProblemPort deleteProblemPort, CountAnswersPort countAnswersPort, TransactionTemplate tx) {
        this.deleteProblemPort = deleteProblemPort;
        this.countAnswersPort = countAnswersPort;
        this.tx = tx;
    }

    @Override
    public DeletedProblemEvent handle(DeleteProblemCommand command) {
        ProblemId problemId = ProblemId.of(command.getId());

        if (countAnswersPort.countByProblemId(problemId) > 0) {
            throw new AlreadyHasAnswersException();
        }

        return tx.execute(status -> {
            deleteProblemPort.delete(problemId);
            return new DeletedProblemEvent(problemId);
        });
    }
}

package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.transaction.support.TransactionTemplate;

@UseCase
class DeleteProblemUseCaseImpl implements DeleteProblemUseCase {
    private final DeleteProblemPort deleteProblemPort;
    private final CountAnswersPort countAnswersPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final TransactionTemplate tx;

    DeleteProblemUseCaseImpl(DeleteProblemPort deleteProblemPort, CountAnswersPort countAnswersPort, CurrentDateTimePort currentDateTimePort, TransactionTemplate tx) {
        this.deleteProblemPort = deleteProblemPort;
        this.countAnswersPort = countAnswersPort;
        this.currentDateTimePort = currentDateTimePort;
        this.tx = tx;
    }

    @Override
    public DeletedProblemEvent handle(DeleteProblemCommand command) throws AlreadyHasAnswersException {
        ProblemId problemId = ProblemId.of(command.getId());
        UserId deleterId = UserId.of(command.getDeleterId());

        if (countAnswersPort.countByProblemId(problemId) > 0) {
            throw new AlreadyHasAnswersException();
        }

        return tx.execute(status -> {
            deleteProblemPort.delete(problemId);
            return new DeletedProblemEvent(problemId.asString(),
                    deleterId.asString(),
                    currentDateTimePort.now());
        });
    }
}

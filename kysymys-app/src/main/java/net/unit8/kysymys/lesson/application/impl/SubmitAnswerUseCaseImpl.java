package net.unit8.kysymys.lesson.application.impl;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.*;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SubmitAnswerUseCaseImpl implements SubmitAnswerUseCase {
    private final SaveAnswerPort saveAnswerPort;
    private final LoadProblemPort loadProblemPort;
    private final TransactionTemplate tx;

    public SubmitAnswerUseCaseImpl(SaveAnswerPort saveAnswerPort, LoadProblemPort loadProblemPort, TransactionTemplate tx) {
        this.saveAnswerPort = saveAnswerPort;
        this.loadProblemPort = loadProblemPort;
        this.tx = tx;
    }

    @Override
    public SubmittedAnswerEvent handle(SubmitAnswerCommand command) {
        Validated<AnswerRepository> answerRepositoryValidated = AnswerRepository.validator.validate(command.getRepositoryUrl(), command.getCommitHash());
        Validated<AnswerId> answerIdValidated = AnswerId.validator.validate(command.getAnswererId());

        Problem problem = ProblemId.validator.validate(command.getProblemId())
                .map(loadProblemPort::load)
                .orElseThrow(ConstraintViolationsException::new)
                .orElseThrow(() -> new ProblemNotFoundException(command.getProblemId()));

        UserId userId = UserId.of(command.getAnswererId());

        Answer answer = answerIdValidated.combine(answerRepositoryValidated).apply((answerId, repository) ->
                Answer.validator.validated(Arguments.of(answerId, problem, userId, repository))
        ).orElseThrow(ConstraintViolationsException::new);

        return tx.execute(status -> {
            saveAnswerPort.save(answer);
            return new SubmittedAnswerEvent(answer.getId());
        });
    }
}

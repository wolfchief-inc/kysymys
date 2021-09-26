package net.unit8.kysymys.lesson.application.impl;

import am.ik.yavi.core.ConstraintViolationsException;
import am.ik.yavi.core.Validated;
import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.*;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class SubmitAnswerUseCaseImpl implements SubmitAnswerUseCase {
    private final SaveAnswerPort saveAnswerPort;
    private final LoadProblemPort loadProblemPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final TransactionTemplate tx;
    private final ApplicationEventPublisher applicationEventPublisher;

    public SubmitAnswerUseCaseImpl(SaveAnswerPort saveAnswerPort, LoadProblemPort loadProblemPort, CurrentDateTimePort currentDateTimePort, TransactionTemplate tx, ApplicationEventPublisher applicationEventPublisher) {
        this.saveAnswerPort = saveAnswerPort;
        this.loadProblemPort = loadProblemPort;
        this.currentDateTimePort = currentDateTimePort;
        this.tx = tx;
        this.applicationEventPublisher = applicationEventPublisher;
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

        LocalDateTime now = currentDateTimePort.now();
        Answer answer = answerIdValidated.combine(answerRepositoryValidated).apply((answerId, repository) ->
                Answer.validator.validated(answerId, problem, userId, repository, now)
        ).orElseThrow(ConstraintViolationsException::new);

        SubmittedAnswerEvent event = tx.execute(status -> {
            saveAnswerPort.save(answer);
            return new SubmittedAnswerEvent(answer.getId().getValue(),
                    problem.getId().getValue(),
                    problem.getName().getValue(),
                    command.getAnswererId(),
                    command.getAnswererName(),
                    command.getFollowers(),
                    now);
        });
        Optional.ofNullable(event)
                .ifPresent(applicationEventPublisher::publishEvent);
        return event;
    }
}

package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.*;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.share.application.GenerateCursorPort;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@Component
class PostCommentUseCaseImpl implements PostCommentUseCase {
    private final LoadAnswerPort loadAnswerPort;
    private final SaveCommentPort saveCommentPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final GenerateCursorPort generateCursorPort;
    private final TransactionTemplate tx;

    PostCommentUseCaseImpl(LoadAnswerPort loadAnswerPort, SaveCommentPort saveCommentPort, CurrentDateTimePort currentDateTimePort, GenerateCursorPort generateCursorPort, TransactionTemplate tx) {
        this.loadAnswerPort = loadAnswerPort;
        this.saveCommentPort = saveCommentPort;
        this.currentDateTimePort = currentDateTimePort;
        this.generateCursorPort = generateCursorPort;
        this.tx = tx;
    }

    @Override
    public PostedCommentEvent handle(PostCommentCommand command) {
        Answer answer = loadAnswerPort.load(AnswerId.of(command.getAnswerId()))
                .orElseThrow(() -> new AnswerNotFoundException(command.getAnswerId()));

        LocalDateTime now = currentDateTimePort.now();
        Comment comment = Comment.of(CommentId.of(generateCursorPort.generateId()), answer, UserId.of(command.getCommenterId()),
                Description.of(command.getDescription()),
                now);
        return tx.execute(status -> {
            saveCommentPort.save(comment);
            return new PostedCommentEvent(answer.getId().getValue(), command.getCommenterId(), now);
        });
    }
}

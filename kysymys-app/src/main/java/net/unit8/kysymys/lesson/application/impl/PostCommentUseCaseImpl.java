package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.*;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
class PostCommentUseCaseImpl implements PostCommentUseCase {
    private final LoadAnswerPort loadAnswerPort;
    private final SaveCommentPort saveCommentPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final TransactionTemplate tx;

    PostCommentUseCaseImpl(LoadAnswerPort loadAnswerPort, SaveCommentPort saveCommentPort, CurrentDateTimePort currentDateTimePort, TransactionTemplate tx) {
        this.loadAnswerPort = loadAnswerPort;
        this.saveCommentPort = saveCommentPort;
        this.currentDateTimePort = currentDateTimePort;
        this.tx = tx;
    }

    @Override
    public PostedCommentEvent handle(PostCommentCommand command) {
        Answer answer = loadAnswerPort.load(AnswerId.of(command.getAnswerId()))
                .orElseThrow(() -> new AnswerNotFound(command.getAnswerId()));

        Comment comment = Comment.of(new CommentId(), answer, UserId.of(command.getCommenterId()),
                Description.of(command.getDescription()),
                currentDateTimePort.now());
        return tx.execute(status -> {
            saveCommentPort.save(comment);
            return new PostedCommentEvent();
        });
    }
}

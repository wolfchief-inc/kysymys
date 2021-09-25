package net.unit8.kysymys.lesson.application.impl;

import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.AnswerId;
import net.unit8.kysymys.lesson.domain.AnswerWithComments;
import net.unit8.kysymys.lesson.domain.Comment;
import net.unit8.kysymys.share.application.GenerateCursorPort;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.domain.UserId;

import java.util.List;

@UseCase
public class ShowAnswerUseCaseImpl implements ShowAnswerUseCase {
    private final LoadAnswerPort loadAnswerPort;
    private final ListCommentPort listCommentPort;
    private final GenerateCursorPort generateCursorPort;

    public ShowAnswerUseCaseImpl(LoadAnswerPort loadAnswerPort, ListCommentPort listCommentPort, GenerateCursorPort generateCursorPort) {
        this.loadAnswerPort = loadAnswerPort;
        this.listCommentPort = listCommentPort;
        this.generateCursorPort = generateCursorPort;
    }

    @Override
    public AnswerWithComments handle(ShowAnswerQuery query) {
        AnswerId answerId = AnswerId.of(query.getAnswerId());
        UserId viewerUserId = UserId.of(query.getViewerUserId());

        Answer answer = loadAnswerPort.load(answerId)
                .orElseThrow(() -> new AnswerNotFoundException(query.getAnswerId()));
        List<Comment> comments = listCommentPort.listRecentComments(answerId, generateCursorPort.generateId(), 10);
        return new AnswerWithComments(answer, comments);
    }
}

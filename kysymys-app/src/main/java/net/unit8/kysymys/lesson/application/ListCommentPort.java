package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.AnswerId;
import net.unit8.kysymys.lesson.domain.Comment;

import java.util.List;

public interface ListCommentPort {
    List<Comment> listRecentComments(AnswerId answerId, String cursor, int size);
}

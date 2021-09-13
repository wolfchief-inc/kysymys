package net.unit8.kysymys.lesson.application;

import net.unit8.kysymys.lesson.domain.Comment;

public interface SaveCommentPort {
    void save(Comment comment);
}

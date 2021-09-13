package net.unit8.kysymys.lesson.application;

public class AnswerNotFound extends RuntimeException {
    public AnswerNotFound(String answerId) {
        super(answerId);
    }
}

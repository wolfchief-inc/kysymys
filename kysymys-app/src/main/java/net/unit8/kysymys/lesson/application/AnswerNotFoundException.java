package net.unit8.kysymys.lesson.application;

public class AnswerNotFoundException extends RuntimeException {
    public AnswerNotFoundException(String answerId) {
        super(answerId);
    }
}

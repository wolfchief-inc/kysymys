package net.unit8.kysymys.lesson.application;

public class AnswerNotFoundException extends Exception {
    public AnswerNotFoundException(String answerId) {
        super(answerId);
    }
}

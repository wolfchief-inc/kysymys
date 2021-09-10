package net.unit8.kysymys.lesson.application;

public class ProblemNotFoundException extends RuntimeException {
    public ProblemNotFoundException(String problemId) {
        super(problemId);
    }
}

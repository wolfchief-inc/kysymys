package net.unit8.kysymys.lesson.application;

public class ProblemNotFoundException extends Exception {
    public ProblemNotFoundException(String problemId) {
        super(problemId);
    }
}

package net.unit8.kysymys.lesson.application;

import lombok.Value;

@Value
public class SubmitAnswerCommand {
    String problemId;
    String answererId;
    String repositoryUrl;
    String commitHash;
}

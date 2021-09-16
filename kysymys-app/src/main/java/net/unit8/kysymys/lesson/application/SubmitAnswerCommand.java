package net.unit8.kysymys.lesson.application;

import lombok.Value;

import java.util.List;

@Value
public class SubmitAnswerCommand {
    String problemId;
    String answererId;
    String answererName;
    List<Follower> followers;
    String repositoryUrl;
    String commitHash;

    @Value
    public static class Follower {
        String id;
        String name;
        String email;
    }
}

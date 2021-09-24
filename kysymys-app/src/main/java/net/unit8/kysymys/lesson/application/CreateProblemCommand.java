package net.unit8.kysymys.lesson.application;

import lombok.Value;

@Value
public class CreateProblemCommand {
    String name;
    String repositoryUrl;
    String branch;
    String readmePath;
    String creatorId;
}

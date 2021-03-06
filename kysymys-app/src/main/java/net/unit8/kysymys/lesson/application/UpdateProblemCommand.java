package net.unit8.kysymys.lesson.application;

import lombok.Value;

@Value
public class UpdateProblemCommand {
    String id;
    String name;
    String repositoryUrl;
    String branch;
    String readmePath;
    String updaterId;
}

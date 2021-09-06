package net.unit8.kysymys.lesson.domain;

import lombok.Value;

@Value
public class ProblemRepository {
    String url;
    String branch;
    String readmePath;
}

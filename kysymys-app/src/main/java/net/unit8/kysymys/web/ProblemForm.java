package net.unit8.kysymys.web;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProblemForm implements Serializable {
    private String name;
    private String repositoryUrl;
    private String branch;
    private String readmePath;
}

package net.unit8.kysymys.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SubmitAnswerRequest implements Serializable {
    @JsonProperty("problem_id")
    private String problemId;
    @JsonProperty("repository_url")
    private String repositoryUrl;
    @JsonProperty("commit_hash")
    private String commitHash;
}

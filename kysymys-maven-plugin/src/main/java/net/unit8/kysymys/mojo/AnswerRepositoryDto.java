package net.unit8.kysymys.mojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;

@Value
public class AnswerRepositoryDto implements Serializable {
    @JsonProperty(value = "problem_id")
    private String problemId;

    @JsonProperty(value = "repository_url")
    private String repositoryUrl;

    @JsonProperty(value = "commit_hash")
    private String commitHash;
}

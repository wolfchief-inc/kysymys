package net.unit8.kysymys.mojo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.io.Serializable;

@Value
public class AnswerRepositoryDto implements Serializable {
    @JsonProperty(value = "problem_id")
    String problemId;

    @JsonProperty(value = "repository_url")
    String repositoryUrl;

    @JsonProperty(value = "commit_hash")
    String commitHash;
}

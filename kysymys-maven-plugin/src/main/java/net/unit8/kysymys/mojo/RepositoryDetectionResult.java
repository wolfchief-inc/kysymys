package net.unit8.kysymys.mojo;

import lombok.Value;

@Value
public class RepositoryDetectionResult {
    String repositoryUrl;
    String commitHash;
}

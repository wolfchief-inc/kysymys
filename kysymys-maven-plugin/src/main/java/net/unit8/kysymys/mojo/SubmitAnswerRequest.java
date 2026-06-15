package net.unit8.kysymys.mojo;

import lombok.Value;

import java.io.Serializable;

/**
 * Request body for {@code POST /problems/{problemId}/answers}:
 * {@code {"repository": {...}, "commitHash": "..."}}.
 *
 * <p>The problem id travels in the path, not the body.
 */
@Value
public class SubmitAnswerRequest implements Serializable {
    AnswerRepositoryDto repository;
    String commitHash;
}

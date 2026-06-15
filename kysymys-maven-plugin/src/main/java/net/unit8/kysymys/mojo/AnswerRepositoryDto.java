package net.unit8.kysymys.mojo;

import lombok.Value;

import java.io.Serializable;

/**
 * The {@code repository} object of a submit-answer request:
 * {@code {"type": "github", "url": "..."}}.
 */
@Value
public class AnswerRepositoryDto implements Serializable {
    String type;
    String url;
}

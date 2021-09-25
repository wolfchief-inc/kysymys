package net.unit8.kysymys.web;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class CommentForm implements Serializable {
    @NotBlank
    private String description;
}

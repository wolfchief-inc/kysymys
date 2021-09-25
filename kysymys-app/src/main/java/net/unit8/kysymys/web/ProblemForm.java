package net.unit8.kysymys.web;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class ProblemForm implements Serializable {
    @NotBlank
    @Length(max = 100)
    private String name;
    @NotBlank
    @Length(max = 100)
    private String repositoryUrl;
    private String branch;
    private String readmePath;
}

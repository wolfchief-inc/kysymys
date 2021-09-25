package net.unit8.kysymys.web;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class SignupForm implements Serializable {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Length(max = 100)
    private String name;

    @NotBlank
    @Length(min = 8, max = 100)
    private String password;
}

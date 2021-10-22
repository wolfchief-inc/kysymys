package net.unit8.kysymys.web;

import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.util.StringUtils;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@Data
public class EditUserForm implements Serializable {
    @NotBlank
    @Length(max = 100)
    String name;

    @Length(max = 100)
    String oldPassword;

    String newPassword;

    @AssertTrue(message = "must be more than 8 characters")
    public boolean isNotNewPasswordTooShort() {
        return !StringUtils.hasText(newPassword) || newPassword.length() >= 8;
    }

    @AssertTrue(message = "must be less than 100 characters")
    public boolean isNotNewPasswordTooLong() {
        return !StringUtils.hasText(newPassword) || newPassword.length() <= 100;
    }
}

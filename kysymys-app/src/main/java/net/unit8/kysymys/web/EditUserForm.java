package net.unit8.kysymys.web;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Objects;

@Data
public class EditUserForm implements Serializable {
    @NotBlank
    @Length(max = 100)
    String name;
    @Length(max = 100)
    String newPassword;
    String oldPassword;

    @AssertTrue(message = "password mismatch")
    public boolean matchPassword() {
        if (newPassword == null || newPassword.isBlank()) {
            return true;
        }
        return Objects.equals(newPassword, oldPassword);
    }
}

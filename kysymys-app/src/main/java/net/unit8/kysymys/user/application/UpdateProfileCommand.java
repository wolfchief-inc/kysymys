package net.unit8.kysymys.user.application;

import lombok.Value;

import java.io.Serializable;

@Value
public class UpdateProfileCommand implements Serializable {
    String userId;
    String name;
    String oldPassword;
    String newPassword;
}

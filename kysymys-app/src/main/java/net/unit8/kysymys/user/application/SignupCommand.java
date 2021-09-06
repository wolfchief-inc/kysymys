package net.unit8.kysymys.user.application;

import lombok.Value;

@Value
public class SignupCommand {
    String email;
    String name;
    String password;
}

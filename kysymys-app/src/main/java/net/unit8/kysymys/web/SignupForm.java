package net.unit8.kysymys.web;

import lombok.Data;

import java.io.Serializable;

@Data
public class SignupForm implements Serializable {
    private String email;
    private String name;
    private String password;
}

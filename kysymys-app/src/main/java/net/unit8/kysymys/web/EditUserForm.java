package net.unit8.kysymys.web;

import lombok.Data;

import java.io.Serializable;

@Data
public class EditUserForm implements Serializable {
    String name;
    String newPassword;
    String oldPassword;
}

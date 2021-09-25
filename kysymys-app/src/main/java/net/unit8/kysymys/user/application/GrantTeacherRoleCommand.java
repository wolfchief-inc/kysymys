package net.unit8.kysymys.user.application;

import lombok.Value;

import java.io.Serializable;

@Value
public class GrantTeacherRoleCommand implements Serializable {
    String granterId;
    String granteeId;
}

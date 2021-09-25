package net.unit8.kysymys.user.domain;

import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
public class TeacherRoleGrantedEvent implements Serializable {
    String granterId;
    String granteeId;
    String granteeName;
    LocalDateTime occurredAt;
}

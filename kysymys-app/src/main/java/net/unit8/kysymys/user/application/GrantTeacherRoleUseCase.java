package net.unit8.kysymys.user.application;

import lombok.Value;

import java.time.LocalDateTime;

public interface GrantTeacherRoleUseCase {
    TeacherRoleGrantedEvent handle(GrantTeacherRoleCommand command);

    @Value
    class GrantTeacherRoleCommand {
        String granterId;
        String granteeId;
    }

    @Value
    class TeacherRoleGrantedEvent {
        String granterId;
        String granteeId;
        String granteeName;
        LocalDateTime occurredAt;
    }
}

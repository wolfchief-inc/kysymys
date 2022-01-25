package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.TeacherRoleGrantedEvent;

public interface GrantTeacherRoleUseCase {
    TeacherRoleGrantedEvent handle(GrantTeacherRoleCommand command);

    @Value
    class GrantTeacherRoleCommand {
        String granterId;
        String granteeId;
    }
}

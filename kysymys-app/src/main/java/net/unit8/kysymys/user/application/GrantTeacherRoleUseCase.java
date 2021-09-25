package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.TeacherRoleGrantedEvent;

public interface GrantTeacherRoleUseCase {
    TeacherRoleGrantedEvent handle(GrantTeacherRoleCommand command);
}

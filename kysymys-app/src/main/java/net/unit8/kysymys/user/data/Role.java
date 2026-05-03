package net.unit8.kysymys.user.data;

import java.util.Set;

public enum Role {
    STUDENT(Set.of(Permission.SUBMIT_ANSWER, Permission.POST_COMMENT)),
    TEACHER(Set.of(Permission.CREATE_PROBLEM, Permission.GRANT_TEACHER_ROLE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> permissions() {
        return permissions;
    }
}

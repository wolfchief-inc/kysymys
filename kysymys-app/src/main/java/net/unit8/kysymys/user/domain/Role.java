package net.unit8.kysymys.user.domain;

import java.util.Arrays;
import java.util.stream.Collectors;

public enum Role {
    STUDENT("SUBMIT_ANSWER", "POST_COMMENT"),
    TEACHER("CREATE_PROBLEM", "GRANT_TEACHER_ROLE");

    Role(String... values) {
        permissions = Permissions.of(Arrays.stream(values)
                .map(Permission::of)
                .collect(Collectors.toSet()));
    }
    private final Permissions permissions;

    public Permissions getPermissions() {
        return permissions;
    }
}

package net.unit8.kysymys.user.data;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record Roles(Set<Role> values) {
    public Roles {
        Objects.requireNonNull(values, "values");
        values = Collections.unmodifiableSet(EnumSet.copyOf(values.isEmpty() ? Set.of(Role.STUDENT) : values));
    }

    public static Roles of(Set<Role> values) {
        return new Roles(values);
    }

    public static Roles fromNames(Set<String> names) {
        return of(names.stream().map(Role::valueOf).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    public Roles withAdded(Role role) {
        Set<Role> next = EnumSet.copyOf(values);
        next.add(role);
        return new Roles(next);
    }

    public Set<Permission> permissions() {
        Set<Permission> all = EnumSet.noneOf(Permission.class);
        for (Role r : values) all.addAll(r.permissions());
        return Collections.unmodifiableSet(all);
    }

    public boolean contains(Role role) {
        return values.contains(role);
    }
}

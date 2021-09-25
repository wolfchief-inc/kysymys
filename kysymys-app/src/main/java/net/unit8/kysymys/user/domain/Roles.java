package net.unit8.kysymys.user.domain;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class Roles extends AbstractSet<Role> {
    private final Set<Role> value;

    @Override
    public Iterator<Role> iterator() {
        return value.iterator();
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public boolean add(Role role) {
        return value.add(role);
    }

    private Roles(Set<Role> value) {
        this.value = value;
    }

    public static Roles of(Set<String> value) {
        return new Roles(value.stream().map(Role::valueOf).collect(Collectors.toSet()));
    }

    public Permissions getPermissions() {
        return value.stream()
                .map(Role::getPermissions)
                .reduce(Permissions::merge)
                .orElse(Permissions.of(Set.of()));
    }
}

package net.unit8.kysymys.user.domain;

import lombok.EqualsAndHashCode;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EqualsAndHashCode(callSuper = false)
public class Permissions extends AbstractSet<Permission> {
    private final Set<Permission> value;
    @Override
    public Iterator<Permission> iterator() {
        return value.iterator();
    }

    @Override
    public int size() {
        return value.size();
    }

    private Permissions(Set<Permission> value) {
        this.value = value;
    }

    public static Permissions merge(Permissions a, Permissions b) {
        return Permissions.of(Stream.concat(a.value.stream(), b.value.stream())
                .collect(Collectors.toSet()));
    }

    public static Permissions of(Set<Permission> value) {
        return new Permissions(value);
    }
}

package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.*;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ValueValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class User implements UserDetails {
    public static final Arguments5Validator<UserId, EmailAddress, UserName, Password, Roles, User> validator = ArgumentsValidatorBuilder.of(User::new)
            .builder(b -> b._object(Arguments1::arg1, "userId", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "email", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "name", c -> c.notNull()))
            .builder(b -> b._object(Arguments4::arg4, "password", c -> c.notNull()))
            .builder(b -> b._object(Arguments5::arg5, "roles", c -> c.notNull()))
            .build();

    private final UserId id;
    private final EmailAddress email;
    private final UserName name;
    private final Password password;
    private final Roles roles;

    public static User of(UserId id, EmailAddress email, UserName name, Password password, Roles roles) {
        return validator.validated(id, email, name, password, roles);
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.getPermissions();
    }

    @Override
    public String getPassword() {
        return password.getValue();
    }

    @Override
    public String getUsername() {
        return email.toString();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.*;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class User implements UserDetails, OAuth2User {
    public static final Arguments5Validator<UserId, EmailAddress, UserName, Password, Roles, User> validator = ArgumentsValidatorBuilder.of(User::new)
            .builder(b -> b._object(Arguments1::arg1, "userId", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "email", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "name", c -> c.notNull()))
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

    public static User fromOAuth2(UserId id, EmailAddress email, UserName name) {
        return validator.validated(id, email, name, null, Roles.of(Set.of("STUDENT")));
    }

    public String getName() {
        return name.getValue();
    }

    @Override
    public <A> A getAttribute(String name) {
        return null;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.getPermissions();
    }

    @Override
    public String getPassword() {
        return Optional.ofNullable(password)
                .map(Password::getValue)
                .orElse(null);
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

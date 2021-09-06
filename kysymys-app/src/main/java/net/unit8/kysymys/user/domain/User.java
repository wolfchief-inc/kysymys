package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.Arguments;
import am.ik.yavi.arguments.Arguments4;
import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.core.ValueValidator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class User implements UserDetails {
    public static final ValueValidator<Arguments4<UserId, EmailAddress, UserName, Password>, User> validator = ValidatorBuilder.of(User.class)
            .build()
            .applicative()
            .compose(args -> new User(args.arg1(), args.arg2(), args.arg3(), args.arg4()));

    private final UserId id;
    private final EmailAddress email;
    private final UserName name;
    private final Password password;

    public static User of(UserId id, EmailAddress email, UserName name, Password password) {
        return validator.validated(Arguments.of(id, email, name, password));
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
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

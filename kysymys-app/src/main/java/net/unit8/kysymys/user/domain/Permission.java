package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.StringValidator;
import am.ik.yavi.builder.StringValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.security.core.GrantedAuthority;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Permission implements GrantedAuthority {
    public static StringValidator<Permission> validator = StringValidatorBuilder.of("authority",
            c -> c.notBlank().lessThanOrEqual(100))
            .build()
            .andThen(Permission::new);
    private final String authority;

    public static Permission of(String authority) {
        return validator.validated(authority);
    }

    @Override
    public String getAuthority() {
        return authority;
    }

    @Override
    public String toString() {
        return authority;
    }
}

package net.unit8.kysymys.inject;

import enkan.web.data.HttpRequest;
import kotowari.inject.ParameterInjector;
import net.unit8.kysymys.user.data.UserId;
import org.jspecify.annotations.Nullable;

import java.security.Principal;

/**
 * Resolves {@code UserId} parameters on resource methods. Reads the principal
 * (set by {@code AuthenticationMiddleware}) and constructs a {@link UserId}
 * from its name (the {@code sub} JWT claim).
 */
public class UserIdInjector implements ParameterInjector<UserId> {
    @Override
    public String getName() {
        return "userId";
    }

    @Override
    public boolean isApplicable(Class<?> type) {
        return UserId.class.isAssignableFrom(type);
    }

    @Override
    public @Nullable UserId getInjectObject(HttpRequest request) {
        Principal principal = request.getPrincipal();
        if (principal == null) return null;
        return UserId.of(principal.getName());
    }
}

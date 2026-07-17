package net.unit8.kysymys.avatar.resource;

import enkan.Endpoint;
import enkan.component.jooq.JooqProvider;
import enkan.web.data.HttpRequest;
import enkan.web.data.HttpResponse;
import jakarta.inject.Inject;
import net.unit8.kysymys.avatar.behavior.EnsureAvatar;
import net.unit8.kysymys.avatar.image.EightBitAvatarGenerator;
import net.unit8.kysymys.user.data.UserId;
import net.unit8.kysymys.user.resource.UserPathDecoders;
import net.unit8.raoh.Ok;
import org.jooq.DSLContext;

import java.io.ByteArrayInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serves {@code GET /users/:id/avatar} as {@code image/png}. Mounted as an
 * {@link Endpoint} (not a resource) because the kotowari-restful pipeline
 * is JSON-only by default and an Endpoint sits before content negotiation.
 */
public class AvatarEndpoint implements Endpoint<HttpRequest, HttpResponse> {

    public static final Pattern PATH = Pattern.compile("^/users/([A-Za-z0-9_-]{21})/avatar/?$");

    private static final EightBitAvatarGenerator GENERATOR = new EightBitAvatarGenerator();

    @Inject
    JooqProvider jooqProvider;

    @Override
    public HttpResponse handle(HttpRequest request) {
        Matcher m = PATH.matcher(request.getUri());
        if (!m.matches()) {
            HttpResponse r = HttpResponse.of("Not Found");
            r.setStatus(404);
            return r;
        }
        if (!(UserPathDecoders.USER_ID.decode(m.group(1)) instanceof Ok<UserId>(var userId))) {
            HttpResponse r = HttpResponse.of("Invalid user id");
            r.setStatus(404);
            return r;
        }

        DSLContext dsl = jooqProvider.getDSLContext();
        byte[] bytes = new EnsureAvatar(dsl, GENERATOR).apply(userId);
        HttpResponse response = HttpResponse.of(new ByteArrayInputStream(bytes));
        response.getHeaders().put("Content-Type", "image/png");
        response.getHeaders().put("Content-Length", String.valueOf(bytes.length));
        response.setStatus(200);
        return response;
    }
}

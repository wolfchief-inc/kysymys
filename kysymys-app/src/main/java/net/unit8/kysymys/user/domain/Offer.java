package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.Value;

@Value
public class Offer {
    private static Arguments2Validator<User, User, Offer> validator = ArgumentsValidatorBuilder.of(Offer::new)
            .builder(b -> b._object(Arguments1::arg1, "offeringUser", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "targetUser", c -> c.notNull()))
            .build();

    User offeringUser;
    User targetUser;

    public static Offer of(User offeringUser, User targetUser) {
        return validator.validated(offeringUser, targetUser);
    }
}

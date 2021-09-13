package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.*;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
public class Offer {
    private static Arguments4Validator<OfferId, User, User, LocalDateTime, Offer> validator = ArgumentsValidatorBuilder.of(Offer::new)
            .builder(b -> b._object(Arguments1::arg1, "id", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "offeringUser", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "targetUser", c -> c.notNull()))
            .builder(b -> b._object(Arguments4::arg4, "offeredAt", c -> c.notNull()))
            .build();

    OfferId id;
    User offeringUser;
    User targetUser;
    LocalDateTime offeredAt;

    public static Offer of(OfferId id, User offeringUser, User targetUser, LocalDateTime offeredAt) {
        return validator.validated(id, offeringUser, targetUser, offeredAt);
    }
}

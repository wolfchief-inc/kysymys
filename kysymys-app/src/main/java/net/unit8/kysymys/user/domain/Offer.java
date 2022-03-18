package net.unit8.kysymys.user.domain;

import am.ik.yavi.arguments.*;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.LocalDateTime;

@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Offer {
    private static Arguments4Validator<OfferId, User, User, LocalDateTime, Offer> validator = ArgumentsValidatorBuilder.of(Offer::new)
            .builder(b -> b._object(Arguments1::arg1, "id", c -> c.notNull()))
            .builder(b -> b._object(Arguments2::arg2, "offeringUser", c -> c.notNull()))
            .builder(b -> b._object(Arguments3::arg3, "targetUser", c -> c.notNull()))
            .builder(b -> b._object(Arguments4::arg4, "offeredAt", c -> c.notNull()))
            .build();

    @Getter
    private final OfferId id;
    @Getter
    private final User offeringUser;
    @Getter
    private final User targetUser;
    @Getter
    private final LocalDateTime offeredAt;

    public static Offer of(OfferId id, User offeringUser, User targetUser, LocalDateTime offeredAt) {
        return validator.validated(id, offeringUser, targetUser, offeredAt);
    }
}

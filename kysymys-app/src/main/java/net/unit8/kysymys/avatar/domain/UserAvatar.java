package net.unit8.kysymys.avatar.domain;

import am.ik.yavi.arguments.Arguments1;
import am.ik.yavi.arguments.Arguments2;
import am.ik.yavi.arguments.Arguments2Validator;
import am.ik.yavi.builder.ArgumentsValidatorBuilder;
import lombok.Value;
import net.unit8.kysymys.user.domain.UserId;

@Value
public class UserAvatar {
    private static Arguments2Validator<UserId, byte[], UserAvatar> validator = ArgumentsValidatorBuilder.of(UserAvatar::new)
            .builder(b -> b._object(Arguments1::arg1, "userId", c -> c.notNull()))
            .builder(b -> b._byteArray(Arguments2::arg2, "image", c -> c.notEmpty()))
            .build();

    UserId userId;
    byte[] image;

    public static UserAvatar of(UserId userId, byte[] image) {
        return validator.validated(userId, image);
    }
}

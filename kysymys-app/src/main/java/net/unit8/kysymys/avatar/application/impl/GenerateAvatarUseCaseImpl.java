package net.unit8.kysymys.avatar.application.impl;

import net.unit8.kysymys.avatar.application.*;
import net.unit8.kysymys.avatar.domain.UserAvatar;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;

@UseCase
class GenerateAvatarUseCaseImpl implements GenerateAvatarUseCase {
    private final GenerateAvatarPort generateAvatarPort;
    private final SaveAvatarPort saveAvatarPort;
    private final TransactionTemplate tx;

    GenerateAvatarUseCaseImpl(GenerateAvatarPort generateAvatarPort, SaveAvatarPort saveAvatarPort, TransactionTemplate tx) {
        this.generateAvatarPort = generateAvatarPort;
        this.saveAvatarPort = saveAvatarPort;
        this.tx = tx;
    }

    @Override
    public AvatarGeneratedEvent handle(GenerateAvatarCommand command) {
        byte[] image = generateAvatarPort.generate();
        UserAvatar userAvatar = UserAvatar.of(UserId.of(command.getUserId()), image);
        return tx.execute(status -> {
            saveAvatarPort.save(userAvatar);
            return new AvatarGeneratedEvent(new ByteArrayInputStream(image));
        });
    }
}

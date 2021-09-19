package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.Password;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import net.unit8.kysymys.user.domain.UserName;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@UseCase
class UpdateProfileUseCaseImpl implements UpdateProfileUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final TransactionTemplate tx;

    UpdateProfileUseCaseImpl(LoadUserPort loadUserPort, SaveUserPort saveUserPort, TransactionTemplate tx) {
        this.loadUserPort = loadUserPort;
        this.saveUserPort = saveUserPort;
        this.tx = tx;
    }

    @Override
    public ProfileUpdatedEvent handle(UpdateProfileCommand command) {
        UserId userId = UserId.of(command.getUserId());
        User user = loadUserPort.load(userId)
                .orElseThrow(() -> new UserNotFoundException(userId.getValue()));

        Password newPassword = Optional.ofNullable(command.getNewPassword())
                .filter(s -> !s.isBlank())
                .map(Password::ofRaw)
                .orElseGet(() -> Optional.ofNullable(user.getPassword())
                        .map(Password::ofEncoded)
                        .orElse(Password.notSet()));

        return tx.execute(status -> {
            saveUserPort.save(User.of(
                    userId,
                    user.getEmail(),
                    UserName.of(command.getName()),
                    newPassword,
                    user.getRoles()
            ));
            return new ProfileUpdatedEvent();
        });
    }
}

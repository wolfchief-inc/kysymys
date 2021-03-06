package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;

@UseCase
class SignupUseCaseImpl implements SignupUseCase {
    private final ExistsEmailAddressPort existsEmailAddressPort;
    private final SaveUserPort saveUserPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final TransactionTemplate tx;

    SignupUseCaseImpl(ExistsEmailAddressPort existsEmailAddressPort, SaveUserPort saveUserPort, CurrentDateTimePort currentDateTimePort, PasswordEncoder passwordEncoder, ApplicationEventPublisher applicationEventPublisher, TransactionTemplate tx) {
        this.existsEmailAddressPort = existsEmailAddressPort;
        this.saveUserPort = saveUserPort;
        this.currentDateTimePort = currentDateTimePort;
        this.passwordEncoder = passwordEncoder;
        this.applicationEventPublisher = applicationEventPublisher;
        this.tx = tx;
    }

    @Override
    public UserCreatedEvent handle(SignupCommand command) {
        EmailAddress email = EmailAddress.of(command.getEmail());
        if (existsEmailAddressPort.exists(email)) {
            throw new EmailAlreadyTakenException(command.getEmail());
        }

        Password password = Password.ofEncoded(passwordEncoder.encode(command.getPassword()));
        User user = User.of(
                new UserId(),
                email,
                UserName.of(command.getName()),
                password,
                Roles.of(Set.of("STUDENT")));

        return tx.execute(status -> {
            saveUserPort.save(user);
            UserCreatedEvent event = new UserCreatedEvent(user.getId().getValue(), currentDateTimePort.now());
            applicationEventPublisher.publishEvent(event);
            return event;
        });
    }
}

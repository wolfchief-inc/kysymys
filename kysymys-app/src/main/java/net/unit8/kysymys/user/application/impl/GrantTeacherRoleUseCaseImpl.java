package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.share.application.CurrentDateTimePort;
import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.Role;
import net.unit8.kysymys.user.domain.TeacherRoleGrantedEvent;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.transaction.support.TransactionTemplate;

@UseCase
class GrantTeacherRoleUseCaseImpl implements GrantTeacherRoleUseCase {
    private final LoadUserPort loadUserPort;
    private final SaveUserPort saveUserPort;
    private final CurrentDateTimePort currentDateTimePort;
    private final TransactionTemplate tx;

    GrantTeacherRoleUseCaseImpl(LoadUserPort loadUserPort, SaveUserPort saveUserPort, CurrentDateTimePort currentDateTimePort, TransactionTemplate tx) {
        this.loadUserPort = loadUserPort;
        this.saveUserPort = saveUserPort;
        this.currentDateTimePort = currentDateTimePort;
        this.tx = tx;
    }

    @Override
    public TeacherRoleGrantedEvent handle(GrantTeacherRoleCommand command) {
        UserId granteeId = UserId.of(command.getGranteeId());
        User grantee = loadUserPort.load(granteeId)
                .orElseThrow(() -> new UserNotFoundException(granteeId.getValue()));
        grantee.grantRole(Role.TEACHER);

        return tx.execute(status -> {
            saveUserPort.save(grantee);
            return new TeacherRoleGrantedEvent(command.getGranterId(), command.getGranteeId(), grantee.getName(), currentDateTimePort.now());
        });
    }
}

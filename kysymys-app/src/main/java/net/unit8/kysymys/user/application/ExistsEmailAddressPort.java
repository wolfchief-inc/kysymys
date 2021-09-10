package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.EmailAddress;

public interface ExistsEmailAddressPort {
    boolean exists(EmailAddress emailAddress);
}

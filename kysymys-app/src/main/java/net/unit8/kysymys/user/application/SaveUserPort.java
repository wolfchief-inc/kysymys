package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.User;

public interface SaveUserPort {
    void save(User user);
}

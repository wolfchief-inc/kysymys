package net.unit8.kysymys.avatar.adapter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAvatarRepository extends JpaRepository<UserAvatarJpaEntity, String> {
}

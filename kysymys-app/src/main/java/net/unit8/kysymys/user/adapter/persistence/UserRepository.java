package net.unit8.kysymys.user.adapter.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity, String>, JpaSpecificationExecutor<UserJpaEntity> {
    Optional<UserJpaEntity> findByEmail(String email);

    @Query("SELECT f FROM user u INNER JOIN u.followers f WHERE u.id=:userId")
    Page<UserJpaEntity> findAllFollowers(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT u FROM user u WHERE u.id IN (:ids)")
    Set<UserJpaEntity> findAllByUserIds(@Param("ids") Set<String> ids);

    @Query("SELECT case when count(u) > 0 then true else false end FROM user u JOIN u.followers f WHERE u.id=:followerId AND f.id=:followeeId")
    boolean isFollower(@Param("followerId") String followerId,
                       @Param("followeeId") String followeeId);
}
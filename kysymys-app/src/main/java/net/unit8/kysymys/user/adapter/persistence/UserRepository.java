package net.unit8.kysymys.user.adapter.persistence;

import net.unit8.kysymys.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<UserJpaEntity, String> {
    Optional<UserJpaEntity> findByEmail(String email);

    @Query("SELECT u FROM user u WHERE name like %?#{escape([0])}% escape ?#{escapeCharacter()} OR email LIKE %?#{escape([0])}% escape ?#{escapeCharacter()}")
    Page<UserJpaEntity> findByQuery(String query, Pageable pageable);

    @Query("SELECT f FROM user u INNER JOIN u.followers f WHERE u.id=:userId")
    List<UserJpaEntity> findAllFollowers(@Param("userId") String userId);

    @Query("SELECT u FROM user u WHERE u.id IN (:ids)")
    Set<UserJpaEntity> findAllByUserIds(@Param("ids") Set<String> ids);

    @Query("SELECT case when count(u) > 0 then true else false end FROM user u JOIN u.followers f WHERE u.id=:followerId AND f.id=:followeeId")
    boolean isFollower(@Param("followerId") String followerId,
                       @Param("followeeId") String followeeId);
}
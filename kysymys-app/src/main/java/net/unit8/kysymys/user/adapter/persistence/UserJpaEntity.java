package net.unit8.kysymys.user.adapter.persistence;

import lombok.Data;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity(name = "user")
@Table(name = "users")
@Data
public class UserJpaEntity {
    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column
    private String password;

    @ManyToMany
    @JoinTable(name = "connections",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "followee_id"))
    private Set<UserJpaEntity> followers;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles")
    private Set<String> roles;

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

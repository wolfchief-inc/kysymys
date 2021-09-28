package net.unit8.kysymys.user.adapter.persistence;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.util.Set;

public class UserSpecs {
    public static Specification<UserJpaEntity> containingQuery(String q) {
        String likeParam = "%" + q.replace("_", "\\_").replace("%", "\\%") + "%";
        return (root, query, builder) -> builder.or(
                builder.like(root.get("name"), likeParam, '\\'),
                builder.like(root.get("email"), likeParam, '\\')
            );
    }

    public static Specification<UserJpaEntity> hasRole(Set<String> role) {
        return (root, query, builder) -> {
            Join<UserJpaEntity, String> roles = root.join("roles");
            query.distinct(true);
            return roles.in(role);
        };
    }
}

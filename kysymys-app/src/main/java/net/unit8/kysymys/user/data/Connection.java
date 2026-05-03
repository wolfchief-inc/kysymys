package net.unit8.kysymys.user.data;

import java.util.Objects;

/**
 * A directed follow relation: {@code follower} follows {@code followee}.
 * The legacy schema kept this in a {@code connections} table with
 * {@code (followee_id, follower_id)} as a composite primary key.
 */
public record Connection(UserId followee, UserId follower) {
    public Connection {
        Objects.requireNonNull(followee, "followee");
        Objects.requireNonNull(follower, "follower");
    }
}

package net.unit8.kysymys.user.application;

import lombok.Value;

@Value
public class SearchUsersQuery {
    String query;
    int page;
}

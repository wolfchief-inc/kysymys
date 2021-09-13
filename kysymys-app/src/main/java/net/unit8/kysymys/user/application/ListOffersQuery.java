package net.unit8.kysymys.user.application;

import lombok.Value;

@Value
public class ListOffersQuery {
    String userId;
    int page;
    int size;
}

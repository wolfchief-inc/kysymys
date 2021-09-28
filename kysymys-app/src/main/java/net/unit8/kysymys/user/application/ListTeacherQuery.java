package net.unit8.kysymys.user.application;

import lombok.Value;

import java.io.Serializable;

@Value
public class ListTeacherQuery implements Serializable {
    String query;
    int page;
}

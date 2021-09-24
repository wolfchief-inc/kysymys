package net.unit8.kysymys.lesson.application;

import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
public class ListMyAnswersQuery implements Serializable {
    String userId;
    List<String> problemIds;
}

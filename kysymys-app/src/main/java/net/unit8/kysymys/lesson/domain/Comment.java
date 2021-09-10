package net.unit8.kysymys.lesson.domain;

import lombok.Value;
import net.unit8.kysymys.user.domain.UserId;

@Value
public class Comment {
    UserId commenterId;
    Description description;
}

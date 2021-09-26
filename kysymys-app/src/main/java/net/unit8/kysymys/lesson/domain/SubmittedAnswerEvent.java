package net.unit8.kysymys.lesson.domain;

import lombok.Value;
import net.unit8.kysymys.lesson.application.SubmitAnswerCommand;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Value
public class SubmittedAnswerEvent implements Serializable {
    String answerId;
    String problemId;
    String problemName;
    String answererId;
    String answererName;
    List<SubmitAnswerCommand.Follower> followers;
    LocalDateTime occurredAt;
}

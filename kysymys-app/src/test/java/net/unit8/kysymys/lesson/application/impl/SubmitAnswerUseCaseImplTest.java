package net.unit8.kysymys.lesson.application.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.lesson.application.LoadProblemPort;
import net.unit8.kysymys.lesson.application.SaveAnswerPort;
import net.unit8.kysymys.lesson.application.SubmitAnswerCommand;
import net.unit8.kysymys.lesson.application.SubmitAnswerUseCase;
import net.unit8.kysymys.lesson.domain.*;
import net.unit8.kysymys.share.application.CurrentDateTimePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

class SubmitAnswerUseCaseImplTest {
    SubmitAnswerUseCase sut;
    SaveAnswerPort saveAnswerPort;
    LoadProblemPort loadProblemPort;
    CurrentDateTimePort currentDateTimePort;
    TransactionTemplate tx;
    ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    void setup() {
        saveAnswerPort = mock(SaveAnswerPort.class);
        loadProblemPort = mock(LoadProblemPort.class);
        currentDateTimePort = LocalDateTime::now;
        applicationEventPublisher = mock(ApplicationEventPublisher.class);

        PlatformTransactionManager tm = mock(PlatformTransactionManager.class);
        tx = new TransactionTemplate(tm);
        sut = new SubmitAnswerUseCaseImpl(
                saveAnswerPort,
                loadProblemPort,
                currentDateTimePort,
                tx,
                applicationEventPublisher);
    }

    @Test
    void test() {
        String problemId = NanoIdUtils.randomNanoId();
        Problem problem = Problem.of(
                ProblemId.of(problemId),
                ProblemName.of("problem1"),
                ProblemRepository.of("https://github.com/kawasima/problem1.git", "main")
        );
        when(loadProblemPort.load(any())).thenReturn(Optional.of(problem));
        String answererId = NanoIdUtils.randomNanoId();
        SubmitAnswerCommand command = new SubmitAnswerCommand(
                problemId,
                answererId,
                "answerer",
                List.of(),
                "https://github.com/kawasima/answer1.git",
                "0123456789012345678901234567890123456789"
        );
        SubmittedAnswerEvent event = sut.handle(command);
        verify(saveAnswerPort).save(any());
    }

}
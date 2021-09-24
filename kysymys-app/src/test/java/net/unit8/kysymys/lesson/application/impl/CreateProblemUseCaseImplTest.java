package net.unit8.kysymys.lesson.application.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.unit8.kysymys.lesson.application.CreateProblemCommand;
import net.unit8.kysymys.lesson.application.CreateProblemUseCase;
import net.unit8.kysymys.lesson.application.SaveProblemPort;
import net.unit8.kysymys.lesson.domain.CreatedProblemEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.Mockito.*;

class CreateProblemUseCaseImplTest {
    CreateProblemUseCase sut;
    SaveProblemPort saveProblemPort;
    TransactionTemplate tx;

    @BeforeEach
    void setup() {
        saveProblemPort = mock(SaveProblemPort.class);
        PlatformTransactionManager tm = mock(PlatformTransactionManager.class);
        tx = new TransactionTemplate(tm);
        sut = new CreateProblemUseCaseImpl(
                saveProblemPort,
                tx
        );
    }
     @Test
    void test() {
         CreateProblemCommand command = new CreateProblemCommand(
                 "problem1",
                 "https://github.com/kawasima/problem1.git",
                 "main",
                 "/README.md",
                 NanoIdUtils.randomNanoId()
         );
         CreatedProblemEvent event = sut.handle(command);
         verify(saveProblemPort).save(any());
     }
}
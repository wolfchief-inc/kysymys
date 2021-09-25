package net.unit8.kysymys.web;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.lesson.application.AnswerNotFoundException;
import net.unit8.kysymys.lesson.application.ProblemNotFoundException;
import net.unit8.kysymys.user.application.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class KysymysExceptionHandler {
    @ExceptionHandler(value = UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFound(Exception ex, Model model) {
        model.addAttribute("id", ex.getMessage());
        return "error/userNotFound";
    }

    @ExceptionHandler(value = ProblemNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleProblemNotFound(Exception ex, Model model) {
        model.addAttribute("id", ex.getMessage());
        return "error/problemNotFound";
    }

    @ExceptionHandler(value = AnswerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleAnswerNotFound(Exception ex, Model model) {
        model.addAttribute("id", ex.getMessage());
        return "error/answerNotFound";
    }

    @ExceptionHandler(value = ConstraintViolationsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleConstraintViolationNotFound(Exception ex, Model model) {
        model.addAttribute("violations", ((ConstraintViolationsException)ex).violations());
        return "error/constraintViolations";
    }

}

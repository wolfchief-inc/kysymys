package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.AnswerNotFoundException;
import net.unit8.kysymys.lesson.application.PostCommentUseCase;
import net.unit8.kysymys.lesson.application.PostCommentUseCase.PostCommentCommand;
import net.unit8.kysymys.lesson.application.ShowAnswerUseCase;
import net.unit8.kysymys.lesson.application.ShowAnswerUseCase.ShowAnswerQuery;
import net.unit8.kysymys.lesson.domain.AnswerWithComments;
import net.unit8.kysymys.lesson.domain.Comment;
import net.unit8.kysymys.user.application.ListUsersUseCase;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/answer/{answerId}")
public class AnswerController {
    @Autowired
    private ShowAnswerUseCase showAnswerUseCase;

    @Autowired
    private PostCommentUseCase postCommentUseCase;

    @Autowired
    private ListUsersUseCase listUsersUseCase;

    @GetMapping("")
    public ModelAndView index(@PathVariable("answerId") String answerId,
                              @AuthenticationPrincipal User user,
                              ModelAndView mv) {
        try {
            AnswerWithComments answerWithComments = showAnswerUseCase.handle(new ShowAnswerQuery(answerId, user.getId().getValue()));
            mv.addObject("answer", answerWithComments.getAnswer());
            mv.addObject("comments", answerWithComments.getComments());
            Set<String> userIds = Stream.concat(answerWithComments.getComments()
                            .stream()
                            .map(Comment::getCommenterId), Stream.of(answerWithComments.getAnswer().getAnswererId()))
                    .map(UserId::getValue)
                    .collect(Collectors.toSet());
            Map<UserId, User> usersMap = listUsersUseCase.handle(userIds)
                    .stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            mv.addObject("usersMap", usersMap);
            mv.setViewName("answer/index");
        } catch (AnswerNotFoundException e) {
            mv.setViewName("error/answer_not_found");
        }

        return mv;
    }

    @PostMapping("/comment")
    public ModelAndView postComment(@PathVariable("answerId") String answerId,
                              @AuthenticationPrincipal User user,
                              @Validated CommentForm form,
                              ModelAndView mv) {
        try {
            postCommentUseCase.handle(new PostCommentCommand(answerId, user.getId().getValue(), form.getDescription()));
            mv.setViewName("redirect:/answer/" + answerId);
        } catch (AnswerNotFoundException e) {
            mv.setViewName("error/answer_not_found");
        }
        return mv;
    }
}

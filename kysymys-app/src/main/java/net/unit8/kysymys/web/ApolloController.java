package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.SubmitAnswerCommand;
import net.unit8.kysymys.lesson.application.SubmitAnswerUseCase;
import net.unit8.kysymys.user.application.GetFollowersPort;
import net.unit8.kysymys.user.application.ListFollowersUseCase;
import net.unit8.kysymys.user.application.ShowUserProfileUseCase;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/token")
public class ApolloController {
    @Autowired
    private SubmitAnswerUseCase submitAnswerUseCase;

    @Autowired
    private ListFollowersUseCase listFollowersUseCase;

    private final MultiValueMap<String, DeferredResult<User>> watchRequests = new LinkedMultiValueMap<>();

    @PostMapping(value = "/watch/{token}")
    public DeferredResult<User> watch(@PathVariable("token") String token,
                                        @RequestBody SubmitAnswerRequest submitAnswer) {
        DeferredResult<User> deferredResult = new DeferredResult<>();
        deferredResult.onCompletion(() -> {
            User user = (User) deferredResult.getResult();
            List<User> followers = listFollowersUseCase.handle(user.getId().getValue());
            submitAnswerUseCase.handle(new SubmitAnswerCommand(submitAnswer.getProblemId(),
                    user.getId().getValue(),
                    user.getName(),
                    followers.stream().map(u -> new SubmitAnswerCommand.Follower(
                            u.getId().getValue(), u.getName(), u.getEmail().getValue()
                    )).collect(Collectors.toList()),
                    submitAnswer.getRepositoryUrl(),
                    submitAnswer.getCommitHash()));
            watchRequests.remove(token, deferredResult);
        });
        watchRequests.add(token, deferredResult);
        return deferredResult;
    }

    @GetMapping("/publish/{token}")
    public Object publish(@PathVariable("token") String token,
                          @AuthenticationPrincipal User user
                          ) {
        if (watchRequests.containsKey(token)) {
            Collection<DeferredResult<User>> deferredResults = watchRequests.get(token);
            for (DeferredResult<User> deferredResult : deferredResults) {
                deferredResult.setResult(user);
            }
        }
        return "success";
    }
}

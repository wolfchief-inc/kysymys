package net.unit8.kysymys.web;

import lombok.Value;
import net.unit8.kysymys.lesson.application.SubmitAnswerCommand;
import net.unit8.kysymys.lesson.application.SubmitAnswerUseCase;
import net.unit8.kysymys.user.application.ListFollowersUseCase;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/token")
public class ApolloController {
    @Autowired
    private SubmitAnswerUseCase submitAnswerUseCase;

    @Autowired
    private ListFollowersUseCase listFollowersUseCase;

    private final MultiValueMap<String, DeferredResult<UserDto>> watchRequests = new LinkedMultiValueMap<>();

    @PostMapping(value = "/watch/{token}")
    public DeferredResult<UserDto> watch(@PathVariable("token") String token,
                                         @RequestBody SubmitAnswerRequest submitAnswer) {
        DeferredResult<UserDto> deferredResult = new DeferredResult<>();
        deferredResult.onCompletion(() -> {
            UserDto user = (UserDto) deferredResult.getResult();
            List<User> followers = listFollowersUseCase.handle(user.getId());
            submitAnswerUseCase.handle(new SubmitAnswerCommand(submitAnswer.getProblemId(),
                    user.getId(),
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
            Collection<DeferredResult<UserDto>> deferredResults = watchRequests.get(token);
            for (DeferredResult<UserDto> deferredResult : deferredResults) {
                deferredResult.setResult(new UserDto(user.getId().getValue(), user.getName(), user.getEmail().getValue()));
            }
        }
        return "success";
    }

    @Value
    private static class UserDto implements Serializable {
        String id;
        String name;
        String email;
    }

}

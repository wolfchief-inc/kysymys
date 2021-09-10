package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.SubmitAnswerCommand;
import net.unit8.kysymys.lesson.application.SubmitAnswerUseCase;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;

@RestController
@RequestMapping("/token")
public class ApolloController {
    @Autowired
    private SubmitAnswerUseCase submitAnswerUseCase;

    private MultiValueMap<String, DeferredResult<String>> watchRequests = new LinkedMultiValueMap<>();

    @PostMapping(value = "/watch/{token}")
    public DeferredResult<String> watch(@PathVariable("token") String token,
                                        @RequestBody SubmitAnswerRequest submitAnswer) {
        DeferredResult<String> deferredResult = new DeferredResult<>();
        deferredResult.onCompletion(() -> {
            submitAnswerUseCase.handle(new SubmitAnswerCommand(submitAnswer.getProblemId(),
                    deferredResult.getResult().toString(),
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
            Collection<DeferredResult<String>> deferredResults = watchRequests.get(token);
            for (DeferredResult<String> deferredResult : deferredResults) {
                deferredResult.setResult(user.getId().getValue());
            }
        }
        return "success";
    }
}

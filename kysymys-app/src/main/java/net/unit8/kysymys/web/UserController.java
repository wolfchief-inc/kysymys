package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.ListFollowerAnswersQuery;
import net.unit8.kysymys.lesson.application.ListFollowerAnswersUseCase;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.user.application.SearchUsersQuery;
import net.unit8.kysymys.user.application.SearchUsersUseCase;
import net.unit8.kysymys.user.application.ShowUserProfileQuery;
import net.unit8.kysymys.user.application.ShowUserProfileUseCase;
import net.unit8.kysymys.user.domain.FollowStatus;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserProfileByOther;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private ShowUserProfileUseCase showUserProfileUseCase;
    @Autowired
    private SearchUsersUseCase searchUsersUseCase;
    @Autowired
    private ListFollowerAnswersUseCase listFollowerAnswersUseCase;

    @GetMapping("/search")
    public String userSearch(@RequestParam(value = "q", required = false) String q,
                             @RequestParam(value = "p", required = false, defaultValue = "1") int page,
                             Model model) {
        SearchUsersQuery query = new SearchUsersQuery(q, page);
        Page<User> users = searchUsersUseCase.handle(query);
        model.addAttribute("users", users);
        int totalPages = users.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "user/list";
    }

    @GetMapping("/{userId:^(?!search).*}")
    public String user(@PathVariable("userId") String userId,
                       @AuthenticationPrincipal User user,
                       Model model) {
        UserProfileByOther profile = showUserProfileUseCase.handle(new ShowUserProfileQuery(
                userId,
                user.getId().getValue()
        ));

        model.addAttribute("user", profile.getUser());
        model.addAttribute("followers", profile.getFollower());
        model.addAttribute("isMyProfile", Objects.equals(userId, user.getId().getValue()));
        model.addAttribute("followStatus", profile.getFollowStatus().orElse(null));

        if (Objects.equals(userId, user.getId().getValue()) || profile.getFollowStatus().filter(s -> s == FollowStatus.FOLLOWING).isPresent()) {
            Page<Answer> answers = listFollowerAnswersUseCase.handle(new ListFollowerAnswersQuery(userId));
            model.addAttribute("answers", answers);
        }

        return "user/profile";
    }
}

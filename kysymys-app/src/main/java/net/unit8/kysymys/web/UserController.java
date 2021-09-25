package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.ListFollowerAnswersQuery;
import net.unit8.kysymys.lesson.application.ListFollowerAnswersUseCase;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.user.application.*;
import net.unit8.kysymys.user.domain.FollowStatus;
import net.unit8.kysymys.user.domain.TeacherRoleGrantedEvent;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserProfileByOther;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
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
    @Autowired
    private GrantTeacherRoleUseCase grantTeacherRoleUseCase;
    @Autowired
    private UpdateProfileUseCase updateProfileUseCase;

    @Autowired
    private MessageSource messageSource;

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

    @GetMapping("/{userId:^(?!search).*}/edit")
    public String edit(@PathVariable("userId") String userId,
                       @AuthenticationPrincipal User user,
                       Model model) {
        model.addAttribute("userId", userId);
        if (!model.containsAttribute("editUserForm")) {
            UserProfileByOther profile = showUserProfileUseCase.handle(new ShowUserProfileQuery(
                    userId,
                    user.getId().getValue()
            ));
            EditUserForm form = new EditUserForm();
            form.setName(user.getName());
            model.addAttribute("editUserForm", form);
        }

        return "user/edit";
    }

    @PostMapping("/{userId:^(?!search).*}/edit")
    public String update(@PathVariable("userId") String userId,
                       @AuthenticationPrincipal User user,
                       @Validated EditUserForm form,
                       BindingResult bindingResult,
                       Model model) {
        if (bindingResult.hasErrors()) {
            return edit(userId, user, model);
        }
        updateProfileUseCase.handle(new UpdateProfileCommand(
                userId,
                form.getName(),
                form.getNewPassword(),
                form.getOldPassword()
        ));
        return "redirect:/user/" + userId;
    }

    @PostMapping("/{userId:^(?!search).*}/grant/teacher")
    public String grantTeacherRole(@PathVariable("userId") String userId,
                                   @AuthenticationPrincipal User user,
                                   RedirectAttributes redirectAttributes,
                                   Locale locale) {
        TeacherRoleGrantedEvent event = grantTeacherRoleUseCase.handle(new GrantTeacherRoleCommand(
                user.getId().getValue(),
                userId
        ));
        redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                "message.teacher_role_granted",
                new Object[]{ event.getGranteeName() },
                locale
        ));
        return "redirect:/user/" + userId;
    }

}

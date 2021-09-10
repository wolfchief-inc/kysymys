package net.unit8.kysymys.web;

import net.unit8.kysymys.user.application.ShowUserProfileQuery;
import net.unit8.kysymys.user.application.ShowUserProfileUseCase;
import net.unit8.kysymys.user.domain.User;
import net.unit8.kysymys.user.domain.UserProfileByOther;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class UserController {
    private ShowUserProfileUseCase showUserProfileUseCase;
    @GetMapping("/{userId}")
    public String user(@RequestParam("userId") String userId,
                       @AuthenticationPrincipal User user,
                       Model model) {
        UserProfileByOther profile = showUserProfileUseCase.handle(new ShowUserProfileQuery(
                userId,
                user.getId().getValue()
        ));
        model.addAttribute("user", profile.getUser());
        model.addAttribute("followers", profile.getFollower());
        model.addAttribute("isFollowing", profile.isFollowing());
        return "user/profile";
    }
}

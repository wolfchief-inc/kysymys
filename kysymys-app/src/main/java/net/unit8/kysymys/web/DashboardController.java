package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.ListProblemsQuery;
import net.unit8.kysymys.lesson.application.ListProblemsUseCase;
import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    @Autowired
    private ListProblemsUseCase listProblemsUseCase;

    @GetMapping("/")
    public String index(Model model,
                        @AuthenticationPrincipal User user) {
        Page<Problem> problems = listProblemsUseCase.handle(new ListProblemsQuery(0, 5));
        model.addAttribute("problems", problems);
        return "index";
    }
}

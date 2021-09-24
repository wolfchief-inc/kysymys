package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.ListMyAnswersQuery;
import net.unit8.kysymys.lesson.application.ListMyAnswersUseCase;
import net.unit8.kysymys.lesson.application.ListProblemsQuery;
import net.unit8.kysymys.lesson.application.ListProblemsUseCase;
import net.unit8.kysymys.lesson.domain.Answer;
import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.lesson.domain.ProblemId;
import net.unit8.kysymys.notification.application.GetWhatsNewsQuery;
import net.unit8.kysymys.notification.application.GetWhatsNewsUseCase;
import net.unit8.kysymys.notification.domain.WhatsNew;
import net.unit8.kysymys.user.application.ListOffersQuery;
import net.unit8.kysymys.user.application.ListOffersUseCase;
import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    @Autowired
    private GetWhatsNewsUseCase getWhatsNewsUseCase;

    @Autowired
    private ListProblemsUseCase listProblemsUseCase;

    @Autowired
    private ListMyAnswersUseCase listMyAnswersUseCase;

    @Autowired
    private ListOffersUseCase listOffersUseCase;

    @GetMapping("/")
    public String index(Model model,
                        @AuthenticationPrincipal User user) {
        Page<WhatsNew> whatsNews = getWhatsNewsUseCase.handle(new GetWhatsNewsQuery(user.getId().getValue(), 5));
        model.addAttribute("whatsNews", whatsNews);

        Page<Problem> problems = listProblemsUseCase.handle(new ListProblemsQuery(0, 5));
        model.addAttribute("problems", problems);

        Map<ProblemId, Answer> answers = listMyAnswersUseCase.handle(new ListMyAnswersQuery(user.getId().getValue(), problems
                .stream()
                .map(Problem::getId)
                .map(ProblemId::getValue)
                .collect(Collectors.toList())))
                .stream()
                .collect(Collectors.toMap(a -> a.getProblem().getId(), a -> a));
        model.addAttribute("answers", answers);

        Page<Offer> offers = listOffersUseCase.handle(new ListOffersQuery(user.getId().getValue(), 0, 5));
        model.addAttribute("offers", offers);
        return "index";
    }
}

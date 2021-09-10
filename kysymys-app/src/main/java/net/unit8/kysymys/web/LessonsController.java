package net.unit8.kysymys.web;

import net.unit8.kysymys.lesson.application.ListProblemsQuery;
import net.unit8.kysymys.lesson.application.ListProblemsUseCase;
import net.unit8.kysymys.lesson.domain.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/lesson")
public class LessonsController {
    @Autowired
    private ListProblemsUseCase listProblemsUseCase;

    @GetMapping("")
    public String list(@RequestParam(value = "p", defaultValue = "1", required = false) int page,
                       Model model) {
        Page<Problem> problems = listProblemsUseCase.handle(new ListProblemsQuery(page, 10));
        model.addAttribute("problems", problems);
        int totalPages = problems.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "lesson/list";
    }
}

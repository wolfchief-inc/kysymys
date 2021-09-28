package net.unit8.kysymys.web;

import net.unit8.kysymys.user.application.ListTeacherQuery;
import net.unit8.kysymys.user.application.ListTeachersUseCase;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequestMapping("/teachers")
@Controller
public class TeachersController {
    @Autowired
    private ListTeachersUseCase listTeachersUseCase;

    @RequestMapping("")
    public String index(@RequestParam(value = "q", required = false) String q,
                        @RequestParam(value = "p", required = false, defaultValue = "1") int page,
                        Model model) {
        Page<User> teachers = listTeachersUseCase.handle(new ListTeacherQuery(q, page));
        model.addAttribute("teachers", teachers);
        int totalPages = teachers.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }
        return "teacher/list";
    }
}

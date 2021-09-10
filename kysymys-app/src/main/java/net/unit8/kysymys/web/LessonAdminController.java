package net.unit8.kysymys.web;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.CreatedProblemEvent;
import net.unit8.kysymys.lesson.domain.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin/lesson")
public class LessonAdminController {
    @Autowired
    private CreateProblemUseCase createProblemUseCase;

    @Autowired
    private GetProblemUseCase getProblemUseCase;

    @ModelAttribute
    private ProblemForm setupForm() {
        return new ProblemForm();
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        return "admin/lesson/new";
    }

    @PostMapping("/new")
    public String create(@Validated ProblemForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            return newForm(model);
        }

        try {
            CreateProblemCommand command = new CreateProblemCommand(
                    form.getName(),
                    form.getRepositoryUrl(),
                    form.getBranch(), form.getReadmePath());
            CreatedProblemEvent event = createProblemUseCase.handle(command);
            model.addAttribute("problemId", event.getProblemId());
            return "redirect:/lesson";
        } catch (ConstraintViolationsException e) {
            e.violations().forEach(violation -> {
                bindingResult.addError(new FieldError(bindingResult.getObjectName(),
                        violation.name(),
                        violation.messageKey()));
            });
            return newForm(model);
        }
    }

    @GetMapping("/edit/{problemId}")
    public String edit(@PathVariable("problemId") String problemId,
                       Model model,
                       HttpServletResponse response) {
        Problem problem = getProblemUseCase.handle(problemId);
        model.addAttribute(problem);
        return "admin/lesson/edit";
    }

    @PostMapping("/edit/{problemId}")
    public String update(@PathVariable("problemId") String problemId,
                         @Validated ProblemForm form,
                         BindingResult bindingResult,
                         Model model) {
        Problem problem = getProblemUseCase.handle(problemId);
        return "redirect:/lesson";
    }
}

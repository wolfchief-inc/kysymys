package net.unit8.kysymys.web;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.domain.CreatedProblemEvent;
import net.unit8.kysymys.lesson.domain.Problem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;
import java.util.Optional;

@Controller
@RequestMapping("/admin/lesson")
public class LessonAdminController {
    @Autowired
    private CreateProblemUseCase createProblemUseCase;

    @Autowired
    private UpdateProblemUseCase updateProblemUseCase;

    @Autowired
    private DeleteProblemUseCase deleteProblemUseCase;

    @Autowired
    private GetProblemUseCase getProblemUseCase;

    @Autowired
    private MessageSource messageSource;

    @ModelAttribute(name = "form")
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
                         RedirectAttributes redirectAttributes,
                         Locale locale,
                         Model model) {
        Optional.ofNullable(form.getRepositoryUrl())
                .map(String::trim)
                .map(u -> u.replaceAll("[/]+$", ""))
                .ifPresent(form::setRepositoryUrl);
        Optional.ofNullable(form.getReadmePath())
                .map(String::trim)
                .map(p -> p.replaceAll("^[/]+", ""))
                .ifPresent(form::setReadmePath);

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
            redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                    "message.createdProblem",
                    new Object[]{ event.getProblemId() },
                    locale
            ));
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
                       Model model) {
        Problem problem = getProblemUseCase.handle(problemId);
        ProblemForm form = Optional.ofNullable(model.getAttribute("form"))
                .filter(ProblemForm.class::isInstance)
                .map(ProblemForm.class::cast)
                .orElseGet(ProblemForm::new);
        form.setName(problem.getName().getValue());
        form.setRepositoryUrl(problem.getRepository().getUrl());
        form.setBranch(problem.getRepository().getBranch());
        form.setReadmePath(problem.getRepository().getReadmePath());
        model.addAttribute("problemId", problemId);

        return "admin/lesson/edit";
    }

    @PostMapping("/edit/{problemId}")
    public String update(@PathVariable("problemId") String problemId,
                         @Validated ProblemForm form,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes,
                         Locale locale,
                         Model model) {
        Problem problem = getProblemUseCase.handle(problemId);
        UpdatedProblemEvent event = updateProblemUseCase.handle(new UpdateProblemCommand(
                problemId,
                form.getName(),
                form.getRepositoryUrl(),
                form.getBranch(),
                form.getReadmePath()
        ));
        redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                "message.updatedProblem",
                new Object[]{ event.getProblemId() },
                locale
        ));
        return "redirect:/lesson";
    }

    @PostMapping("/delete/{problemId}")
    public String delete(@PathVariable("problemId") String problemId,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        try {
            DeletedProblemEvent event = deleteProblemUseCase.handle(new DeleteProblemCommand(problemId));
            redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                    "message.deletedProblem",
                    new Object[]{event.getProblemId()},
                    locale
            ));
        } catch (AlreadyHasAnswersException e) {
            redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                    "message.fail_to_delete_problem",
                    new Object[]{},
                    locale
            ));
        }
        return "redirect:/lesson";
    }
}

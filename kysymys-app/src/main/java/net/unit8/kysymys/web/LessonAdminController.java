package net.unit8.kysymys.web;

import am.ik.yavi.core.ConstraintViolationsException;
import net.unit8.kysymys.lesson.application.*;
import net.unit8.kysymys.lesson.application.CreateProblemUseCase.CreateProblemCommand;
import net.unit8.kysymys.lesson.application.CreateProblemUseCase.CreatedProblemEvent;
import net.unit8.kysymys.lesson.application.DeleteProblemUseCase.DeletedProblemEvent;
import net.unit8.kysymys.lesson.application.UpdateProblemUseCase.UpdateProblemCommand;
import net.unit8.kysymys.lesson.domain.Problem;
import net.unit8.kysymys.lesson.domain.ProblemUpdatedEvent;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
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

    @ModelAttribute(name = "problemForm")
    private ProblemForm setupForm() {
        return new ProblemForm();
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Optional.ofNullable(model.getAttribute("problemForm"))
                .filter(ProblemForm.class::isInstance)
                .map(ProblemForm.class::cast)
                .filter(f -> f.getReadmePath() != null && f.getReadmePath().startsWith("/"))
                .ifPresent(f -> f.setReadmePath(f.getReadmePath().substring(1)));
        return "admin/lesson/new";
    }

    private void normalize(ProblemForm form) {
        Optional.ofNullable(form.getRepositoryUrl())
                .map(String::trim)
                .map(u -> u.replaceAll("[/]+$", ""))
                .ifPresent(form::setRepositoryUrl);
        Optional.ofNullable(form.getReadmePath())
                .map(String::trim)
                .map(p -> p.replaceAll("^([/]+|(?!/))", "/"))
                .ifPresent(form::setReadmePath);
    }
    @PostMapping("/new")
    public String create(@Validated ProblemForm form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal User user,
                         RedirectAttributes redirectAttributes,
                         Locale locale,
                         Model model) {
        normalize(form);
        if (bindingResult.hasErrors()) {
            return newForm(model);
        }

        try {
            CreateProblemCommand command = new CreateProblemCommand(
                    form.getName(),
                    form.getRepositoryUrl(),
                    form.getBranch(), form.getReadmePath(),
                    user.getId().asString());
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
    public ModelAndView edit(@PathVariable("problemId") String problemId,
                       ModelAndView mv) {
        try {
            Problem problem = getProblemUseCase.handle(problemId);
            ProblemForm form = Optional.ofNullable(mv.getModelMap().getAttribute("problemForm"))
                    .filter(ProblemForm.class::isInstance)
                    .map(ProblemForm.class::cast)
                    .orElseGet(ProblemForm::new);
            form.setName(problem.getName().asString());
            form.setRepositoryUrl(problem.getRepository().getUrl());
            form.setBranch(problem.getRepository().getBranch());
            form.setReadmePath(problem.getRepository().getReadmePath().substring(1));
            mv.addObject("problemId", problemId);
            mv.setViewName("admin/lesson/edit");
        } catch (ProblemNotFoundException e) {
            mv.setViewName("error/problem_not_found");
            mv.setStatus(HttpStatus.NOT_FOUND);
        }
        return mv;
    }

    @PostMapping("/edit/{problemId}")
    public ModelAndView update(@PathVariable("problemId") String problemId,
                         @Validated ProblemForm form,
                         BindingResult bindingResult,
                         @AuthenticationPrincipal User user,
                         RedirectAttributes redirectAttributes,
                         Locale locale,
                         ModelAndView mv) {
        normalize(form);
        if (bindingResult.hasErrors()) {
            return edit(problemId, mv);
        }
        ProblemUpdatedEvent event = updateProblemUseCase.handle(new UpdateProblemCommand(
                problemId,
                form.getName(),
                form.getRepositoryUrl(),
                form.getBranch(),
                form.getReadmePath(),
                user.getId().asString()
        ));
        redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                "message.updatedProblem",
                new Object[]{ event.getProblemId() },
                locale
        ));
        mv.setViewName("redirect:/lesson");
        return mv;
    }

    @PostMapping("/delete/{problemId}")
    public String delete(@PathVariable("problemId") String problemId,
                         @AuthenticationPrincipal User user,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        try {
            DeletedProblemEvent event = deleteProblemUseCase.handle(new DeleteProblemUseCase.DeleteProblemCommand(problemId, user.getId().asString()));
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

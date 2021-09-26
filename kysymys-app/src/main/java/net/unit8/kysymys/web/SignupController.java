package net.unit8.kysymys.web;

import net.unit8.kysymys.user.application.EmailAlreadyTakenException;
import net.unit8.kysymys.user.application.SignupCommand;
import net.unit8.kysymys.user.application.SignupUseCase;
import net.unit8.kysymys.user.domain.UserCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SignupController {
    @Autowired
    private SignupUseCase signupUseCase;

    @GetMapping("/signup")
    public String signupForm(Model model) {
        if (!model.containsAttribute("signupForm")) {
            model.addAttribute("signupForm", new SignupForm());
        }
        return "signup";
    }

    @PostMapping
    public String signup(@Validated SignupForm signupForm, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return signupForm(model);
        }

        try {
            UserCreatedEvent event = signupUseCase.handle(new SignupCommand(
                    signupForm.getEmail(),
                    signupForm.getName(),
                    signupForm.getPassword()
            ));
            model.addAttribute("userId", event.getUserId());
            return "redirect:/login";
        } catch (EmailAlreadyTakenException e) {
            bindingResult.addError(new FieldError(bindingResult.getObjectName(), "email", "Email has already taken"));
            return signupForm(model);
        }
    }
}

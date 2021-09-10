package net.unit8.kysymys.web;

import net.unit8.kysymys.user.application.AcceptFollowCommand;
import net.unit8.kysymys.user.application.AcceptFollowUseCase;
import net.unit8.kysymys.user.application.OfferToFollowCommand;
import net.unit8.kysymys.user.application.OfferToFollowUseCase;
import net.unit8.kysymys.user.domain.OfferedToFollowEvent;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
@RequestMapping("/offer")
public class OfferController {
    @Autowired
    private OfferToFollowUseCase offerToFollowUseCase;

    @Autowired
    private AcceptFollowUseCase acceptFollowUseCase;

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/{targetUserId}")
    public String offer(@AuthenticationPrincipal User user,
                        @RequestParam String targetUserId,
                        RedirectAttributes redirectAttributes,
                        Locale locale) {
        OfferedToFollowEvent event = offerToFollowUseCase.handle(new OfferToFollowCommand(user.getId().getValue(), targetUserId));
        redirectAttributes.addFlashAttribute("message", messageSource.getMessage(
                "message.offeredToFollow",
                new Object[]{},
                locale
        ));
        return "redirect:/user/" + targetUserId;
    }

    @PostMapping("/accept/{offerId}")
    public String accept(@AuthenticationPrincipal User user,
                         @RequestParam String offerId,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        acceptFollowUseCase.handle(new AcceptFollowCommand(offerId, user.getId().getValue()));
        return "redirect:/";
    }

}

package net.unit8.kysymys.web;

import net.unit8.kysymys.user.application.AcceptFollowUseCase;
import net.unit8.kysymys.user.application.AcceptFollowUseCase.AcceptFollowCommand;
import net.unit8.kysymys.user.application.OfferNotFound;
import net.unit8.kysymys.user.application.OfferToFollowUseCase;
import net.unit8.kysymys.user.application.OfferToFollowUseCase.OfferToFollowCommand;
import net.unit8.kysymys.user.application.OfferToFollowUseCase.OfferedToFollowEvent;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @PostMapping("/send/{targetUserId}")
    public String offer(@AuthenticationPrincipal User user,
                        @PathVariable("targetUserId") String targetUserId,
                        RedirectAttributes redirectAttributes,
                        Locale locale) {
        OfferedToFollowEvent event = offerToFollowUseCase.handle(new OfferToFollowCommand(user.getId().asString(), targetUserId));
        redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                "message.offeredToFollow",
                new Object[]{ event.getTargetUserName() },
                locale
        ));
        return "redirect:/user/" + targetUserId;
    }

    @PostMapping("/accept/{offerId}")
    public String accept(@AuthenticationPrincipal User user,
                         @PathVariable("offerId") String offerId,
                         RedirectAttributes redirectAttributes,
                         Locale locale) {
        try {
            acceptFollowUseCase.handle(new AcceptFollowCommand(offerId, user.getId().asString()));
            return "redirect:/";
        } catch (OfferNotFound e) {
            return "redirect:/error/offer_not_found";
        }
    }

}

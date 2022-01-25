package net.unit8.kysymys.web;

import net.unit8.kysymys.notification.application.MarkAsReadUseCase;
import net.unit8.kysymys.notification.application.MarkAsReadUseCase.MarkAsReadCommand;
import net.unit8.kysymys.notification.domain.MarkedAsReadEvent;
import net.unit8.kysymys.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/whatsNew")
public class WhatsNewController {
    @Autowired
    private MarkAsReadUseCase markAsReadUseCase;

    @Autowired
    private MessageSource messageSource;

    @PostMapping("/markAsRead")
    public String markAsRead(@RequestParam("whatsNewId") List<String> whatsNewIds,
                             @AuthenticationPrincipal User user,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        MarkedAsReadEvent event = markAsReadUseCase.handle(new MarkAsReadCommand(user.getId().getValue(),
                whatsNewIds));
        redirectAttributes.addFlashAttribute("notification", messageSource.getMessage(
                "message.marked_as_read",
                new Object[]{  },
                locale
        ));

        return "redirect:/";
    }
}

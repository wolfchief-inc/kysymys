package net.unit8.kysymys.web;

import net.unit8.kysymys.avatar.application.AvatarNotFoundException;
import net.unit8.kysymys.avatar.application.GenerateAvatarUseCase;
import net.unit8.kysymys.avatar.application.GenerateAvatarUseCase.AvatarGeneratedEvent;
import net.unit8.kysymys.avatar.application.GenerateAvatarUseCase.GenerateAvatarCommand;
import net.unit8.kysymys.avatar.application.GetAvatarImageUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Controller
@RequestMapping("/avatar/{userId}")
public class AvatarController {
    @Autowired
    private GetAvatarImageUseCase getAvatarImageUseCase;

    @Autowired
    private GenerateAvatarUseCase generateAvatarUseCase;

    @GetMapping(value = "" , produces = "image/png")
    public ResponseEntity<InputStreamResource> index(@PathVariable("userId") String userId) {
        InputStream avatarStream;
        try {
            avatarStream = new ByteArrayInputStream(getAvatarImageUseCase.handle(userId));
        } catch(AvatarNotFoundException e) {
            AvatarGeneratedEvent event = generateAvatarUseCase.handle(new GenerateAvatarCommand(userId));
            avatarStream = event.getImage();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(avatarStream));
    }

    @PutMapping(value = "", produces = "image/png")
    public ResponseEntity<InputStreamResource> generate(@PathVariable("userId") String userId) {
        AvatarGeneratedEvent event = generateAvatarUseCase.handle(new GenerateAvatarCommand(userId));
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(event.getImage()));
    }
}

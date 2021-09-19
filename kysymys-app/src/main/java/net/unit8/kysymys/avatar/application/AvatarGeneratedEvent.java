package net.unit8.kysymys.avatar.application;

import lombok.Value;

import java.io.InputStream;
import java.io.Serializable;

@Value
public class AvatarGeneratedEvent implements Serializable {
    InputStream image;
}

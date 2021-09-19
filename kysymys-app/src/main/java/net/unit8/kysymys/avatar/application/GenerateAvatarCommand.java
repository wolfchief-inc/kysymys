package net.unit8.kysymys.avatar.application;

import lombok.Value;

import java.io.Serializable;

@Value
public class GenerateAvatarCommand implements Serializable {
    String userId;
}

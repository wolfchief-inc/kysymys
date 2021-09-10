package net.unit8.kysymys.user.application;

import lombok.Value;

import java.io.Serializable;

@Value
public class ShowUserProfileQuery implements Serializable {
    String targetUserId;
    String viewerUserId;
}

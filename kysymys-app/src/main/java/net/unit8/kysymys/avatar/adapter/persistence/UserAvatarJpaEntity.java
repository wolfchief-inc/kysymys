package net.unit8.kysymys.avatar.adapter.persistence;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "userAvatar")
@Table(name = "user_avatars")
@Data
public class UserAvatarJpaEntity implements Serializable {
    @Id
    private String userId;

    @Column(name = "avatar_code")
    private Long avatarCode;

    @Lob
    @Column(name = "image_content")
    private byte[] imageContent;
}

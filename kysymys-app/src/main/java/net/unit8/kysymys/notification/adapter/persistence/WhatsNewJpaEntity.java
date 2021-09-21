package net.unit8.kysymys.notification.adapter.persistence;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "whatsNew")
@Table(name = "whats_news")
@Data
public class WhatsNewJpaEntity implements Serializable {
    @Id
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "template_path", nullable = false)
    private String templatePath;

    @Lob
    @Column(name = "params")
    private String params;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;
}

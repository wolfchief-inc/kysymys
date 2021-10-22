package net.unit8.kysymys.notification.adapter.persistence;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "unreadWhatsNew")
@Table(name = "unread_whats_news")
@ToString
public class UnreadWhatsNewJpaEntity implements Serializable {
    @Id
    @Getter
    @Setter
    private String id;

    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "whats_new_id", nullable = false)
    @Getter
    @Setter
    private WhatsNewJpaEntity whatsNew;

    @Override
    public int hashCode() {
        return Objects.hashCode(whatsNew);
    }
}

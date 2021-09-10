package net.unit8.kysymys.user.adapter.persistence;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "offer")
@Table(name = "offers")
@Data
public class OfferJpaEntity {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "offering_user_id")
    private UserJpaEntity offeringUser;

    @ManyToOne
    @JoinColumn(name = "target_user_id")
    private UserJpaEntity targetUser;

    @Column(name = "offered_at")
    private LocalDateTime offeredAt;
}

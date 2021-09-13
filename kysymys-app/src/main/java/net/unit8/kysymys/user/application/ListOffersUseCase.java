package net.unit8.kysymys.user.application;

import net.unit8.kysymys.user.domain.Offer;
import org.springframework.data.domain.Page;

public interface ListOffersUseCase {
    Page<Offer> handle(ListOffersQuery query);
}

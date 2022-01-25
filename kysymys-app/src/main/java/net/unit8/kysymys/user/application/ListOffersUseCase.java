package net.unit8.kysymys.user.application;

import lombok.Value;
import net.unit8.kysymys.user.domain.Offer;
import org.springframework.data.domain.Page;

public interface ListOffersUseCase {
    Page<Offer> handle(ListOffersQuery query);

    @Value
    class ListOffersQuery {
        String userId;
        int page;
        int size;
    }
}

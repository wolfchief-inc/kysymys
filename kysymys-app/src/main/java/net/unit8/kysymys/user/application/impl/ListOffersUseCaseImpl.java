package net.unit8.kysymys.user.application.impl;

import net.unit8.kysymys.stereotype.UseCase;
import net.unit8.kysymys.user.application.ListOffersPort;
import net.unit8.kysymys.user.application.ListOffersQuery;
import net.unit8.kysymys.user.application.ListOffersUseCase;
import net.unit8.kysymys.user.domain.Offer;
import net.unit8.kysymys.user.domain.UserId;
import org.springframework.data.domain.Page;

@UseCase
class ListOffersUseCaseImpl implements ListOffersUseCase {
    private final ListOffersPort listOffersPort;

    public ListOffersUseCaseImpl(ListOffersPort listOffersPort) {
        this.listOffersPort = listOffersPort;
    }

    @Override
    public Page<Offer> handle(ListOffersQuery query) {
        return listOffersPort.listByTargetUser(UserId.of(query.getUserId()), query.getPage(), query.getSize());
    }
}

package net.unit8.kysymys.user.application;

public class OfferNotFound extends RuntimeException {
    public OfferNotFound(String offerId) {
        super(offerId);
    }
}

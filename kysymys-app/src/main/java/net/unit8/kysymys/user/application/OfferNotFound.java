package net.unit8.kysymys.user.application;

public class OfferNotFound extends Exception {
    public OfferNotFound(String offerId) {
        super(offerId);
    }
}

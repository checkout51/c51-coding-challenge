package com.c51.sedwards.c51challenge.model;

import java.util.List;

public class OfferList {
    private long batch_id;
    private List<Offer> offers;

    public long getBatchId() {
        return batch_id;
    }

    public List<Offer> getOffers() {
        return offers;
    }
}

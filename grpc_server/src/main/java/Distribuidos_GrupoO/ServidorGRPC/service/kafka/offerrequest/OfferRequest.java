package Distribuidos_GrupoO.ServidorGRPC.service.kafka.offerrequest;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationItem;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer.DonationOffer;

import java.util.List;

public class OfferRequest {
    String offerId;
    String offerOrgId;
    String requestOrgId;
    List<DonationItem> donations;


    public OfferRequest() {}

    public OfferRequest(String offerId, String offerOrgId, String requestOrgId, List<DonationItem> donations) {
        this.offerId = offerId;
        this.offerOrgId = offerOrgId;
        this.requestOrgId = requestOrgId;
        this.donations = donations;
    }

    public String getOfferId() {
        return offerId;
    }

    public void setOfferId(String offerId) {
        this.offerId = offerId;
    }

    public String getOfferOrgId() {
        return offerOrgId;
    }

    public void setOfferOrgId(String offerOrgId) {
        this.offerOrgId = offerOrgId;
    }

    public String getRequestOrgId() {
        return requestOrgId;
    }

    public void setRequestOrgId(String requestOrgId) {
        this.requestOrgId = requestOrgId;
    }

    public List<DonationItem> getDonations() {
        return donations;
    }

    public void setDonations(List<DonationItem> donations) {
        this.donations = donations;
    }
}

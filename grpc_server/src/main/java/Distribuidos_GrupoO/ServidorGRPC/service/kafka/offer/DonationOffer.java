package Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationItem;

import java.util.List;

public class DonationOffer {
    private String offerId;
    private String organizationId;
    private List<DonationItem> donations;

    public DonationOffer() {}

    public DonationOffer(String offerId, String organizationId, List<DonationItem> donations) {
        this.offerId = offerId;
        this.organizationId = organizationId;
        this.donations = donations;
    }

    public String getOfferId() { return offerId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public List<DonationItem> getDonations() { return donations; }
    public void setDonations(List<DonationItem> donations) { this.donations = donations; }


}

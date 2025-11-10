package Distribuidos_GrupoO.ServidorGRPC.service.kafka.transfer;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationItem;

import java.util.List;

public class DonationTransfer {
    private String requestId;
    private String donorOrganizationId;
    private String recipientOrganizationId;
    private List<DonationItem> donations;

    public DonationTransfer() {}

    public DonationTransfer(String requestId, String donorOrganizationId, String recipientOrganizationId, List<DonationItem> donations) {
        this.requestId = requestId;
        this.donorOrganizationId = donorOrganizationId;
        this.recipientOrganizationId = recipientOrganizationId;
        this.donations = donations;
    }


    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getDonorOrganizationId() { return donorOrganizationId; }
    public void setDonorOrganizationId(String donorOrganizationId) { this.donorOrganizationId = donorOrganizationId; }

    public String getRecipientOrganizationId() { return recipientOrganizationId; }
    public void setRecipientOrganizationId(String recipientOrganizationId) { this.recipientOrganizationId = recipientOrganizationId; }

    public List<DonationItem> getDonations() { return donations; }
    public void setDonations(List<DonationItem> donations) { this.donations = donations; }
}

package Distribuidos_GrupoO.ServidorGRPC.service.kafka;

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

    public static class DonationItem {
        private String category;
        private String description;
        private String quantity;

        public DonationItem() {}

        public DonationItem(String category, String description, String quantity) {
            this.category = category;
            this.description = description;
            this.quantity = quantity;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getQuantity() { return quantity; }
        public void setQuantity(String quantity) { this.quantity = quantity; }
    }
}

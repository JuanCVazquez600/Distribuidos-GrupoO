package Distribuidos_GrupoO.ServidorGRPC.service.kafka.request;

import java.util.List;

public class DonationRequest {
    private String requestId;
    private String organizationId;
    private List<DonationItem> requestedDonations;

    public DonationRequest() {}

    public DonationRequest(String requestId, String organizationId, List<DonationItem> requestedDonations) {
        this.requestId = requestId;
        this.organizationId = organizationId;
        this.requestedDonations = requestedDonations;
    }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

    public List<DonationItem> getRequestedDonations() { return requestedDonations; }
    public void setRequestedDonations(List<DonationItem> requestedDonations) { this.requestedDonations = requestedDonations; }

    public static class DonationItem {
        private String category;
        private String description;

        public DonationItem() {}
        public DonationItem(String category, String description) {
            this.category = category;
            this.description = description;
        }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}

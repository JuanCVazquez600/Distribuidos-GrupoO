package Distribuidos_GrupoO.ServidorGRPC.service.kafka.cancellation;

public class DonationCancellation {
    private String organizationId;
    private String requestId;

    public DonationCancellation() {}

    public DonationCancellation(String organizationId, String requestId) {
        this.organizationId = organizationId;
        this.requestId = requestId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "DonationCancellation{" +
                "organizationId='" + organizationId + '\'' +
                ", requestId='" + requestId + '\'' +
                '}';
    }
}
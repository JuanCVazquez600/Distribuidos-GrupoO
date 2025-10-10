package Distribuidos_GrupoO.ServidorGRPC.service.kafka.eventcancellation;

public class EventCancellation {
    private String organizationId;
    private String eventId;

    public EventCancellation() {}

    public EventCancellation(String organizationId, String eventId) {
        this.organizationId = organizationId;
        this.eventId = eventId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "EventCancellation{" +
                "organizationId='" + organizationId + '\'' +
                ", eventId='" + eventId + '\'' +
                '}';
    }
}
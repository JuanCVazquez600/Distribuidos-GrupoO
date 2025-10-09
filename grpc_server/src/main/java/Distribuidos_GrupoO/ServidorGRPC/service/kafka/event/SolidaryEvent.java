package Distribuidos_GrupoO.ServidorGRPC.service.kafka.event;

import java.time.LocalDateTime;

public class SolidaryEvent {
    private String organizationId;
    private String eventId;
    private String eventName;
    private String description;
    private LocalDateTime dateTime;

    public SolidaryEvent() {}

    public SolidaryEvent(String organizationId, String eventId, String eventName, String description, LocalDateTime dateTime) {
        this.organizationId = organizationId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.description = description;
        this.dateTime = dateTime;
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

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public String toString() {
        return "SolidaryEvent{" +
                "organizationId='" + organizationId + '\'' +
                ", eventId='" + eventId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", description='" + description + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
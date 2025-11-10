package Distribuidos_GrupoO.ServidorGRPC.model;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity 
@Table(name = "offer_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "offer_id", nullable = false)
    private String offerId;

    @Column(name = "offer_org_id", nullable = false)
    private String offerOrgId;

    @Column(name = "request_org_id", nullable = false)
    private String requestOrgId;

    @Column(name = "donations_json", columnDefinition = "TEXT")
    private String donationsJson;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = java.time.LocalDateTime.now();
        }
    }
}

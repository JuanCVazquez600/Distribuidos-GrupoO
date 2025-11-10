package Distribuidos_GrupoO.ServidorGRPC.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "donation_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonationOfferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "offer_id", nullable = false)
    private String offerId;

    @Column(name = "organization_id", nullable = false)
    private String organizationId;

    @Column(name = "donations_json", columnDefinition = "TEXT")
    private String donationsJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
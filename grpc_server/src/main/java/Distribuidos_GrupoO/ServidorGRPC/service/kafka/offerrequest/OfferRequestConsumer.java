package Distribuidos_GrupoO.ServidorGRPC.service.kafka.offerrequest;

import Distribuidos_GrupoO.ServidorGRPC.model.DonationOfferEntity;
import Distribuidos_GrupoO.ServidorGRPC.model.OfferRequestEntity;
import Distribuidos_GrupoO.ServidorGRPC.repository.DonationOfferRepository;
import Distribuidos_GrupoO.ServidorGRPC.repository.OfferRequestRepository;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.DonationItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


    @Service
    public class OfferRequestConsumer {
        private static final Logger logger = LoggerFactory.getLogger(OfferRequestConsumer.class);

        private final List<OfferRequest> requests = new ArrayList<>();

        @Autowired
        private DonationOfferRepository donationOfferRepo;


        @Autowired
        private OfferRequestRepository repository;
        @Autowired
        private ObjectMapper objectMapper;

        @Value("${spring.organization.id:org-300}")
        private String ourOrganizationId;

        @KafkaListener(topics = "oferta-solicitud", groupId = "oferta-solicitud-group", containerFactory = "offerRequestKafkaListenerContainerFactory")

        public void listen(OfferRequest offer) {
            logger.info("Recibida solicitud de oferta: {}", offer.getOfferId());


            if (!this.isOurOrganization(offer.getOfferOrgId())) {
                logger.error("Validacion rechazada:La oferta no pertenece a nuestra organizacion {}, (esperado: {})", offer.getOfferOrgId(), ourOrganizationId);
                return;
            }


            if (!isMyOffer(offer.getOfferId())) {
                logger.error("La oferta no pertenece a nuestra organizacion: {}", offer.getOfferId());
                return;
            }


            if (!donationsMatch(offer.getOfferId(), offer.getDonations())) {
                logger.error(" Las donaciones no coinciden para oferta: {}", offer.getOfferId());
                return;
            }

            try {
                OfferRequestEntity entity = new OfferRequestEntity();
                entity.setOfferId(offer.getOfferId());
                entity.setOfferOrgId(offer.getOfferOrgId());
                entity.setRequestOrgId(offer.getRequestOrgId());
                entity.setDonationsJson(objectMapper.writeValueAsString(offer.getDonations()));
                repository.save(entity);

                requests.add(offer);
                logger.info(" Solicitud válida guardada: {} (solicitante: {})",
                           offer.getOfferId(), offer.getRequestOrgId());
            } catch (Exception e) {
                logger.error(" Error guardando solicitud de oferta {}: {}",
                            offer.getOfferId(), e.getMessage(), e);
            }
        }

        private boolean isOurOrganization(String orgId) {
            return ourOrganizationId.equals(orgId);
        }


        private boolean isMyOffer(String offerId) {
            return donationOfferRepo.existsByOfferIdAndOrganizationId(offerId, ourOrganizationId);

        }



        private boolean donationsMatch(String offerId, List<DonationItem> requestedDonations) {
            try {
                Optional<DonationOfferEntity> myOffer = donationOfferRepo.findByOfferId(offerId);
                if (myOffer.isEmpty()) {
                    logger.debug("No se encontró la oferta {} en la BD para comparar donaciones", offerId);
                    return false;
                }

                String myDonationsJson = myOffer.get().getDonationsJson();
                if (myDonationsJson == null || myDonationsJson.trim().isEmpty()) {
                    logger.error("JSON de donaciones vacío o nulo para oferta: {}", offerId);
                    return false;
                }
                
                List<DonationItem> myOriginalDonations = convertJsonToDonationList(myDonationsJson);
                if (myOriginalDonations.isEmpty()) {
                    logger.error("Lista de donaciones vacía después del parsing para oferta: {}", offerId);
                    return false;
                }

                boolean matches = compareDonations(myOriginalDonations, requestedDonations);
                return matches;
            } catch (Exception e) {
                logger.error("Error comparando donaciones para oferta {}: {}", offerId, e.getMessage(), e);
                return false;
            }
        }

        // Compara dos listas de donaciones elemento por elemento
        private boolean compareDonations(List<DonationItem> original, List<DonationItem> requested) {
            //  Si el tamaño es diferente, no coinciden
            if (original.size() != requested.size()) {
                logger.debug(" Las listas tienen diferente tamaño: original={}, solicitada={}",
                        original.size(), requested.size());
                return false;
            }

            // ✅ Verificar que cada donación original tenga su equivalente en la lista solicitada
            for (DonationItem originalItem : original) {
                boolean foundMatch = false;
                for (DonationItem requestedItem : requested) {
                    boolean categoryMatch = originalItem.getCategory().equals(requestedItem.getCategory());
                    boolean descriptionMatch = originalItem.getDescription().equals(requestedItem.getDescription());
                    boolean quantityMatch = originalItem.getQuantity().equals(requestedItem.getQuantity());

                    if (categoryMatch && descriptionMatch && quantityMatch) {
                        logger.debug(" COINCIDENCIA: en las unidades");
                        foundMatch = true;
                        break;
                    }
                }

                if (!foundMatch) {
                    logger.debug(" NO SE ENCONTRO COINCIDENCIA CON LAS UNIDADES");
                    return false;
                }
            }

            logger.debug("🎉 Todas las donaciones coinciden perfectamente");
            return true;
        }



        // 🔄 Convierte JSON string a lista de donaciones (función helper simple)
        private List<DonationItem> convertJsonToDonationList(String jsonString) {
            try {
                return objectMapper.readValue(jsonString,
                        new com.fasterxml.jackson.core.type.TypeReference<List<DonationItem>>() {});
            } catch (Exception e) {
                logger.error("Error convirtiendo JSON a lista de donaciones: {}", e.getMessage());
                return new ArrayList<>(); // Retorna lista vacía si hay error
            }
        }
    }




package Distribuidos_GrupoO.ServidorGRPC.config;

import Distribuidos_GrupoO.ServidorGRPC.service.kafka.offer.DonationOffer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.request.DonationRequest;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.cancellation.DonationCancellation;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.transfer.DonationTransfer;
import Distribuidos_GrupoO.ServidorGRPC.service.kafka.event.SolidaryEvent;
import org.springframework.kafka.core.ProducerFactory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConfig {
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ConsumerFactory<String, DonationOffer> donationOfferConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ofertas-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(DonationOffer.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonationOffer> donationOfferKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DonationOffer> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(donationOfferConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DonationRequest> donationRequestConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "solicitudes-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(DonationRequest.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonationRequest> donationRequestKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DonationRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(donationRequestConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DonationTransfer> donationTransferConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "transferencias-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(DonationTransfer.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonationTransfer> donationTransferKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DonationTransfer> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(donationTransferConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, DonationCancellation> donationCancellationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "baja-solicitudes-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(DonationCancellation.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DonationCancellation> donationCancellationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, DonationCancellation> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(donationCancellationConsumerFactory());
        return factory;
    }

    @Bean
    public ConsumerFactory<String, SolidaryEvent> solidaryEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "eventos-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(SolidaryEvent.class, false));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SolidaryEvent> solidaryEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SolidaryEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(solidaryEventConsumerFactory());
        return factory;
    }
}

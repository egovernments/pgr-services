package org.egov.pt.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.service.NotificationService;
import org.egov.pt.web.models.Property;
import org.egov.pt.web.models.PropertyRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class PropertyNotificationConsumer {

    @Autowired
    private NotificationService notificationService;


    @KafkaListener(topics = {"${persister.save.property.topic}","${persister.update.property.topic}"})
    public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        ObjectMapper mapper = new ObjectMapper();
        PropertyRequest propertyRequest = new PropertyRequest();
        try {
            log.info("Consuming record: " + record);
            propertyRequest = mapper.convertValue(record, PropertyRequest.class);
        } catch (final Exception e) {
            log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
        }
        log.info("property Received: "+propertyRequest.getProperties().get(0).getPropertyId());
        notificationService.process(propertyRequest);
    }






}
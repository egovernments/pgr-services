package org.egov.hrms.consumer;

import java.util.HashMap;

import org.egov.hrms.model.Notification;
import org.egov.hrms.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NotificationConsumer {
	
	@Autowired
	private NotificationService service;
	

	@KafkaListener(topics = {"${kafka.topics.notification.hrms}"})

	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			Notification notification = mapper.convertValue(record, Notification.class);
			service.sendNotification(notification.getRequest(), notification.getPwdMap());
		} catch (final Exception e) {
			log.error("Error while listening to value: " + record + " on topic: " + topic + ": " + e);
		}
	}
}

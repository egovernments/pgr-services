package org.egov.pgr.consumer;

import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.pgr.contract.EmailRequest;
import org.egov.pgr.contract.SMSRequest;
import org.egov.pgr.contract.Service;
import org.egov.pgr.contract.ServiceRequest;
import org.egov.pgr.producer.PGRProducer;
import org.egov.pgr.repository.ServiceRequestRepository;
import org.egov.pgr.utils.PGRConstants;
import org.egov.pgr.utils.PGRUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@org.springframework.stereotype.Service
@Slf4j
public class PGRNotificationConsumer {
	
	@Autowired
	private PGRProducer pGRProducer;
			
	@Value("${kafka.topics.notification.sms}")
	private String smsNotifTopic;
	
	@Value("${kafka.topics.notification.email}")
	private String emailNotifTopic;
	
	@Value("${text.for.sms.notification}")
	private String textForNotif;
	
	@Value("${text.for.subject.email.notif}")
	private String subjectForEmail;
	
	@Value("${register.complaint.enabled}")
	private Boolean isRegisterNotifEnabled;
	
	@Value("${assign.complaint.enabled}")
	private Boolean isAssignNotifEnabled;
	
	@Value("${reassign.complaint.enabled}")
	private Boolean isReassignNotifEnabled;
	
	@Value("${reject.complaint.enabled}")
	private Boolean isRejectedNotifEnabled;
	
	@Value("${resolve.complaint.enabled}")
	private Boolean isResolveNotificationEnabled;
	
	@Value("${reopen.complaint.enabled}")
	private Boolean isReopenNotifEnabled;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;
		
	@Autowired
	private PGRUtils pGRUtils;
		
    @KafkaListener(topics = {"${kafka.topics.notification.complaint}"})
    
	public void listen(final HashMap<String, Object> record, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		ObjectMapper mapper = new ObjectMapper();
		ServiceRequest serviceReqRequest = new ServiceRequest();
		try{
			log.info("Consuming record: "+record);
			serviceReqRequest = mapper.convertValue(record, ServiceRequest.class);
		}catch(final Exception e){
			log.error("Error while listening to value: "+record+" on topic: "+topic+": "+e.getMessage());
		}
		process(serviceReqRequest);		
	}
    
    public void process(ServiceRequest serviceReqRequest) {
    	for(Service serviceReq: serviceReqRequest.getServices()) {
    		if(isNotificationEnabled(serviceReq)) {
	    		SMSRequest smsRequest = prepareSMSRequest(serviceReq, serviceReqRequest.getRequestInfo());
	        	log.info("SMS: "+smsRequest.getMessage()+" | MOBILE: "+smsRequest.getMobileNumber());
	        	try {
	        		pGRProducer.push(smsNotifTopic, smsRequest);
	        	}catch(Exception e) {}
/*				if(null != serviceReq.getEmail() && !serviceReq.getEmail().isEmpty()) {
					EmailRequest emailRequest = prepareEmailRequest(serviceReq);
		        	log.info("EMAIL: "+emailRequest.getBody()
		        	+"| SUBJECT: "+emailRequest.getSubject()
		        	+"| ID: "+emailRequest.getEmail());
		        	try {
		        		pGRProducer.push(emailNotifTopic, emailRequest);
		        	}catch(Exception e) {}
				}*/
    		}
		}
    }
    
    public SMSRequest prepareSMSRequest(Service serviceReq, RequestInfo requestInfo) {
		String phone = serviceReq.getPhone();
		String message = getMessageForSMS(serviceReq, requestInfo);
		SMSRequest smsRequest = SMSRequest.builder().mobileNumber(phone).message(message).build();
		
		return smsRequest;
    }
    
    public EmailRequest prepareEmailRequest(Service serviceReq) {
		String email = serviceReq.getEmail();
		StringBuilder subject = new StringBuilder();
		String body = getBodyAndSubForEmail(serviceReq, subject);
		EmailRequest emailRequest = EmailRequest.builder().email(email).subject(subject.toString()).body(body)
				.isHTML(true).build();
		
		return emailRequest;
    }
    
    public String getBodyAndSubForEmail(Service serviceReq, StringBuilder subject) {
    	Map<String, Object> map = new HashMap<>();
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        VelocityContext context = new VelocityContext();
    	map.put("name", serviceReq.getFirstName());
    	map.put("id", serviceReq.getServiceRequestId());
		switch(serviceReq.getStatus()) {
		case NEW:{
        	map.put("status", "submitted");
    		break;
/*		}case INPROGRESS:{
        	map.put("status", "assgined to Mr."+serviceReq.getAssignedTo());
    		break;
		}case CANCELLED:{
        	map.put("status", "re-assgined to Mr."+serviceReq.getAssignedTo());
    		break;*/
		}case REJECTED:{
        	map.put("status", "rejected on "+new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
    		break;
		}case CLOSED:{
        	map.put("status", "resolved on "+new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
    		break;
		}default:
			break;
		}
		
        context.put("params", map);
        Template t = ve.getTemplate(PGRConstants.TEMPLATE_COMPLAINT_EMAIL);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
    	String message = writer.toString();
    	String subjectText = subjectForEmail;
    	subject.append(subjectText.replace("<id>", serviceReq.getServiceRequestId()));
    	
    	return message;    	
    }
    
    public String getMessageForSMS(Service serviceReq, RequestInfo requestInfo) {
    	String message = textForNotif;
    	String serviceType = getServiceType(serviceReq, requestInfo);
		message = message.replace("<complaint_type>", serviceType)
				.replace("<id>", serviceReq.getServiceRequestId()).replace("date", new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
		switch(serviceReq.getStatus()) {
		case NEW:{
    		message = message.replaceAll("<status>", "submitted");
    		break;
/*		}case INPROGRESS:{
    		message = message.replaceAll("<status>", "assgined to Mr."+serviceReq.getAssignedTo());
    		break;
		}case CANCELLED:{
    		message = message.replaceAll("<status>", "re-assgined to Mr."+serviceReq.getAssignedTo());
    		break;*/
		}case REJECTED:{
    		message = message.replaceAll("<status>", "rejected on "+new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
    		break;
		}case CLOSED:{
    		message = message.replaceAll("<status>", "resolved on "+new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
    		break;
		}default:
			break;
		}
		
    	return message;
    	
    }
    
    public String getServiceType(Service serviceReq, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder();
		MdmsCriteriaReq mdmsCriteriaReq = pGRUtils.prepareSearchRequestForServiceType(uri, serviceReq.getTenantId(),
				serviceReq.getServiceCode(), requestInfo);
		List<String> serviceTypes = null;
		try {
			Object result = serviceRequestRepository.fetchResult(uri, mdmsCriteriaReq);
			log.info("service definition name result: "+result);
			serviceTypes = (List<String>) JsonPath.read(result, PGRConstants.JSONPATH_SERVICE_CODES);
			if(null == serviceTypes || serviceTypes.isEmpty())
				return PGRConstants.DEFAULT_COMPLAINT_TYPE;
		}catch(Exception e) {
			return PGRConstants.DEFAULT_COMPLAINT_TYPE;
		}
		
    	return serviceTypes.get(0);
    }
    
    public boolean isNotificationEnabled(Service serviceReq) {
    	boolean isNotifEnabled = false;
		switch(serviceReq.getStatus()) {
		case NEW:{
			if(isRegisterNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case INPROGRESS:{
			if(isAssignNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case CANCELLED:{
			if(isReassignNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case REJECTED:{
			if(isRejectedNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case CLOSED:{
			if(isResolveNotificationEnabled) {
				isNotifEnabled = true;
			}
			break;
		}default:
			break;
		}
		
		return isNotifEnabled;
    }
    
}

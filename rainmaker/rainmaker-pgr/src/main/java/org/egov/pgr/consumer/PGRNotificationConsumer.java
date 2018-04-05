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
import org.egov.pgr.contract.ServiceRequest;
import org.egov.pgr.model.ActionInfo;
import org.egov.pgr.model.Service;
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
	
	@Value("${notification.sms.enabled}")
	private Boolean isSMSNotificationEnabled;
	
	@Value("${notification.email.enabled}")
	private Boolean isEmailNotificationEnabled;
	
	@Value("${open.complaint.enabled}")
	private Boolean isOpenComplaintNotifEnabled;
	
	@Value("${assign.complaint.enabled}")
	private Boolean isAssignNotifEnabled;
	
	@Value("${new.complaint.enabled}")
	private Boolean isNewComplaintNotifEnabled;
	
	@Value("${reject.complaint.enabled}")
	private Boolean isRejectedNotifEnabled;
	
	@Value("${resolve.complaint.enabled}")
	private Boolean isResolveNotificationEnabled;
	
	@Value("${close.complaint.enabled}")
	private Boolean isCloseNotifEnabled;
	
	@Value("${email.template.path}")
	private String emailTemplatePath;
		
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
    			ActionInfo actionInfo = serviceReqRequest.getActionInfo().get(serviceReqRequest.getServices().indexOf(serviceReq));
    			if(isSMSNotificationEnabled) {
    	    		SMSRequest smsRequest = prepareSMSRequest(serviceReq, actionInfo, serviceReqRequest.getRequestInfo());
    	        	log.info("SMS: "+smsRequest.getMessage()+" | MOBILE: "+smsRequest.getMobileNumber());
	        		pGRProducer.push(smsNotifTopic, smsRequest);
    			}
    			if(isEmailNotificationEnabled && (null != serviceReq.getEmail() && !serviceReq.getEmail().isEmpty())) {
    					EmailRequest emailRequest = prepareEmailRequest(serviceReq, actionInfo);
    		        	log.info("EMAIL: "+emailRequest.getBody()+"| SUBJECT: "+emailRequest.getSubject()+"| ID: "+emailRequest.getEmail());
		        		pGRProducer.push(emailNotifTopic, emailRequest);
    			}
    		}
		}
    }
    
    public SMSRequest prepareSMSRequest(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo) {
		String phone = serviceReq.getPhone();
		String message = getMessageForSMS(serviceReq, actionInfo, requestInfo);
		SMSRequest smsRequest = SMSRequest.builder().mobileNumber(phone).message(message).build();
		
		return smsRequest;
    }
    
    public EmailRequest prepareEmailRequest(Service serviceReq, ActionInfo actionInfo) {
		String email = serviceReq.getEmail();
		StringBuilder subject = new StringBuilder();
		String body = getBodyAndSubForEmail(serviceReq, actionInfo, subject);
		EmailRequest emailRequest = EmailRequest.builder().email(email).subject(subject.toString()).body(body)
				.isHTML(true).build();
		
		return emailRequest;
    }
    
    public String getBodyAndSubForEmail(Service serviceReq, ActionInfo actionInfo, StringBuilder subject) {
    	Map<String, Object> map = new HashMap<>();
        VelocityEngine ve = new VelocityEngine();
        ve.init();
        VelocityContext context = new VelocityContext();
    	map.put("name", serviceReq.getFirstName());
    	map.put("id", serviceReq.getServiceRequestId());
		switch(serviceReq.getStatus()) {
		case NEW:{
        	map.put("status", "registered");
    		break;
		}case OPEN:{
        	map.put("status", "registered");
    		break;
		}case ASSIGNED:{
        	map.put("status", "assigned to Mr."+actionInfo.getAssignee());
    		break;
		}case RESOLVED:{
        	map.put("status", "resolved on "+new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
    		break;
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
        Template t = ve.getTemplate(emailTemplatePath);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
    	String message = writer.toString();
    	String subjectText = subjectForEmail;
    	subject.append(subjectText.replace("<id>", serviceReq.getServiceRequestId()));
    	
    	return message;    	
    }
    
    public String getMessageForSMS(Service serviceReq, ActionInfo actionInfo, RequestInfo requestInfo) {
    	String message = textForNotif;
    	String serviceType = getServiceType(serviceReq, requestInfo);
		message = message.replace("<complaint_type>", serviceType)
				.replace("<id>", serviceReq.getServiceRequestId()).replace("date", new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
		switch(serviceReq.getStatus()) {
		case NEW:{
    		message = message.replaceAll("<status>", "registered");
    		break;
		}case OPEN:{
    		message = message.replaceAll("<status>", "registered");
    		break;
		}case ASSIGNED:{
    		message = message.replaceAll("<status>", "assgined to Mr."+actionInfo.getAssignee());
    		break;
		}case REJECTED:{
    		message = message.replaceAll("<status>", "rejected on "+new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
    		break;
		}case RESOLVED:{
    		message = message.replaceAll("<status>", "resolved on "+new Date(serviceReq.getAuditDetails().getCreatedTime()).toString());
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
			serviceTypes = JsonPath.read(result, PGRConstants.JSONPATH_SERVICE_CODES);
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
			if(isNewComplaintNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case OPEN:{
			if(isOpenComplaintNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case ASSIGNED:{
			if(isAssignNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case REJECTED:{
			if(isRejectedNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case CLOSED:{
			if(isCloseNotifEnabled) {
				isNotifEnabled = true;
			}
			break;
		}case RESOLVED:{
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

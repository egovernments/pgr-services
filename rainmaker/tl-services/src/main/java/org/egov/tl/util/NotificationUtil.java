package org.egov.tl.util;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tl.config.TLConfiguration;
import org.egov.tl.producer.Producer;
import org.egov.tl.repository.ServiceRequestRepository;
import org.egov.tl.web.models.SMSRequest;
import org.egov.tl.web.models.TradeLicense;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.egov.tl.util.TLConstants.*;


@Component
@Slf4j
public class NotificationUtil {



    private TLConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    private Producer producer;


    @Autowired
    public NotificationUtil(TLConfiguration config,ServiceRequestRepository serviceRequestRepository,Producer producer) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.producer = producer;
    }


    final String receiptNumberKey = "receiptNumber";

    final String amountPaidKey = "amountPaid";


    /**
     * Creates customized message based on tradelicense
     * @param license The tradeLicense for which message is to be sent
     * @param localizationMessage The messages from localization
     * @return customized message based on tradelicense
     */
    public String getCustomizedMsg(TradeLicense license, String localizationMessage){
        String message = null,messageTemplate;
        switch (license.getStatus()){

            case STATUS_INITIATED :
                messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_INITIATED,localizationMessage);
                message = getInitiatedMsg(license,messageTemplate);
                break;

            case STATUS_APPLIED :
                messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_APPLIED,localizationMessage);
                message = getAppliedMsg(license,messageTemplate);
                break;

            case STATUS_PAID :
                messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_PAID,localizationMessage);
                message = getApprovalPendingMsg(license,messageTemplate);
                break;

            case STATUS_APPROVED :
                messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_APPROVED,localizationMessage);
                message = getApprovedMsg(license,messageTemplate);
                break;

            case STATUS_REJECTED :
                messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_REJECTED,localizationMessage);
                message = getRejectedMsg(license,messageTemplate);
                break;

            case STATUS_CANCELLED :
                messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_CANCELLED,localizationMessage);
                message = getCancelledMsg(license,messageTemplate);
                break;
        }

        return message;
    }


    /**
     * Extracts message for the specific code
     * @param notificationCode The code for which message is required
     * @param localizationMessage The localization messages
     * @return message for the specific code
     */
    private String getMessageTemplate(String notificationCode,String localizationMessage){
        String path = "$..messages[?(@.code==\"{}\")].message";
        path = path.replace("{}",notificationCode);
        String message = null;
        try {
            Object messageObj = JsonPath.parse(localizationMessage).read(path);
            message = ((ArrayList<String>)messageObj).get(0);
        }
        catch (Exception e){
            throw new CustomException("PARSING ERROR","Failed to parse the response using jsonPath: "+path);
        }
        return message;
    }


    /**
     * Returns the uri for the localization call
     * @param tenantId TenantId of the propertyRequest
     * @return The uri for localization search call
     */
    public StringBuilder getUri(String tenantId){

        if(config.getIsLocalizationStateLevel())
            tenantId = tenantId.split("\\.")[0];

        StringBuilder uri = new StringBuilder();
        uri.append(config.getLocalizationHost()).append(config.getLocalizationContextPath())
                .append(config.getLocalizationSearchEndpoint()).append("?")
                .append("locale=").append(TLConstants.NOTIFICATION_LOCALE)
                .append("&tenantId=").append(tenantId)
                .append("&module=").append(TLConstants.MODULE);

        return uri;
    }


    /**
     * Fetches messages from localization service
     * @param tenantId tenantId of the tradeLicense
     * @param requestInfo The requestInfo of the request
     * @return Localization messages for the module
     */
    public String getLocalizationMessages(String tenantId, RequestInfo requestInfo){
        LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(getUri(tenantId),requestInfo);
        String jsonString = new JSONObject(responseMap).toString();
        return jsonString;
    }


    /**
     * Creates customized message for initiate
     * @param license tenantId of the tradeLicense
     * @param message Message from localization for initiate
     * @return customized message for initiate
     */
    private String getInitiatedMsg(TradeLicense license,String message){
      //  message = message.replace("<1>",license.);
        message = message.replace("<2>",license.getTradeName());
        message = message.replace("<3>",license.getApplicationNumber());

       return message;
    }


    /**
     Creates customized message for apply
     * @param license tenantId of the tradeLicense
     * @param message Message from localization for apply
     * @return customized message for apply
     */
    private String getAppliedMsg(TradeLicense license,String message){
      //  message = message.replace("<1>",);
        message = message.replace("<2>",license.getTradeName());
        message = message.replace("<3>",license.getApplicationNumber());

        return message;
    }


    /**
      Creates customized message for submitted
     * @param license tenantId of the tradeLicense
     * @param message Message from localization for submitted
     * @return customized message for submitted
     */
    private String getApprovalPendingMsg(TradeLicense license,String message){
     //   message = message.replace("<1>",);
        message = message.replace("<2>",license.getTradeName());

        return message;
    }


    /**
      Creates customized message for approved
     * @param license tenantId of the tradeLicense
     * @param message Message from localization for approved
     * @return customized message for approved
     */
    private String getApprovedMsg(TradeLicense license,String message){
      //  message = message.replace("<1>",);

        String pattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String expiryDate = simpleDateFormat.format(license.getValidTo());
        message = message.replace("<2>",license.getTradeName());
        message = message.replace("<3>",license.getLicenseNumber());
        message = message.replace("<4>",expiryDate);

        return message;
    }


    /**
     Creates customized message for rejected
     * @param license tenantId of the tradeLicense
     * @param message Message from localization for rejected
     * @return customized message for rejected
     */
    private String getRejectedMsg(TradeLicense license,String message){
      //  message = message.replace("<1>",);
        message = message.replace("<2>",license.getTradeName());

        return message;
    }


    /**
     Creates customized message for cancelled
     * @param license tenantId of the tradeLicense
     * @param message Message from localization for cancelled
     * @return customized message for cancelled
     */
    private String getCancelledMsg(TradeLicense license,String message){
     //   message = message.replace("<1>",);
        message = message.replace("<2>",license.getTradeName());
        message = message.replace("<3>",license.getLicenseNumber());

        return message;
    }


    /**
     * Creates message for completed payment for owners
     * @param valMap The map containing required values from receipt
     * @param localizationMessages Message from localization
     * @return message for completed payment for owners
     */
    public String getOwnerPaymentMsg(Map<String,String> valMap, String localizationMessages){
        String messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_PAYMENT_OWNER,localizationMessages);
        messageTemplate = messageTemplate.replace("<2>",valMap.get(amountPaidKey));
        messageTemplate = messageTemplate.replace("<3>",valMap.get(receiptNumberKey));
        return messageTemplate;
    }


    /**
     * Creates message for completed payment for payer
     * @param valMap The map containing required values from receipt
     * @param localizationMessages Message from localization
     * @return message for completed payment for payer
     */
    public String getPayerPaymentMsg(Map<String,String> valMap, String localizationMessages){
        String messageTemplate = getMessageTemplate(TLConstants.NOTIFICATION_PAYMENT_PAYER,localizationMessages);
        messageTemplate = messageTemplate.replace("<2>",valMap.get(amountPaidKey));
        messageTemplate = messageTemplate.replace("<3>",valMap.get(receiptNumberKey));
        return messageTemplate;
    }




    /**
     * Send the SMSRequest on the SMSNotification kafka topic
     * @param smsRequestList The list of SMSRequest to be sent
     */
    public void sendSMS(List<SMSRequest> smsRequestList){
        if (config.getIsSMSEnabled()) {
            if (CollectionUtils.isEmpty(smsRequestList))
                log.info("Messages from localization couldn't be fetched!");
            for(SMSRequest smsRequest: smsRequestList) {
                producer.push(config.getSmsNotifTopic(), smsRequest);
                log.info("MobileNumber: "+smsRequest.getMobileNumber()+" Messages: "+smsRequest.getMessage());
            }
        }
    }










}

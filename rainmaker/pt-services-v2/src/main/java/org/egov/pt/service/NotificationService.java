package org.egov.pt.service;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.producer.Producer;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.web.models.Property;
import org.egov.pt.web.models.PropertyDetail;
import org.egov.pt.web.models.PropertyRequest;
import org.egov.pt.web.models.SMSRequest;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static org.egov.pt.util.PTConstants.NOTIFICATION_LOCALE;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private Producer producer;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private PropertyConfiguration propertyConfiguration;

    @Autowired
    private PropertyUtil util;

    @Value("${notification.url}")
    private String notificationURL;

    /**
     * Processes the json and send the SMSRequest
     * @param request The propertyRequest for which notification has to be send
     */
    public void process(PropertyRequest request,String topic){
        String tenantId = request.getProperties().get(0).getTenantId();
        StringBuilder uri = util.getUri(tenantId,request.getRequestInfo());
        try{
            String citizenMobileNumber = request.getRequestInfo().getUserInfo().getMobileNumber();
            LinkedHashMap responseMap = (LinkedHashMap)serviceRequestRepository.fetchResult(uri, request.getRequestInfo());
            String jsonString = new JSONObject(responseMap).toString();
            String path = getJsonPath(topic,request.getRequestInfo().getUserInfo().getType());
            Object messageObj = JsonPath.parse(jsonString).read(path);
            String message = ((ArrayList<String>)messageObj).get(0);
            request.getProperties().forEach(property -> {
                String customMessage = getCustomizedMessage(property,message,path);
                Set<String> listOfMobileNumber = getMobileNumbers(property);
                if(request.getRequestInfo().getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
                    listOfMobileNumber.add(citizenMobileNumber);
                List<SMSRequest> smsRequests = getSMSRequests(listOfMobileNumber,customMessage);
                sendSMS(smsRequests);
             });
        }
        catch(Exception e)
        {throw new CustomException("LOCALIZATION ERROR","Unable to get message from localization");}
    }

    /**
     * Returns the message for each specific property depending the kafka topic
     * @param property The property for which the notification has to be sent
     * @param message The standard message format
     * @return The customized message for the given property
     */
    private String getCustomizedMessage(Property property,String message,String path){
        String customMessage = null;
        if(path.contains(PTConstants.NOTIFICATION_CREATE_CODE))
            customMessage = getCustomizedCreateMessage(property,message);
        if(path.contains(PTConstants.NOTIFICATION_UPDATE_CODE))
            customMessage = getCustomizedUpdateMessage(property,message);
        if(path.contains(PTConstants.NOTIFICATION_EMPLOYEE_UPDATE_CODE))
            customMessage = getCustomizedUpdateMessageEmployee(property,message);

        return customMessage;
    }


    /**
     * @param topic The kafka topic from which the json was received
     * @param userType UserType in the requestInfo
     * @return JsonPath to fetch the message
     */
    private String getJsonPath(String topic,String userType){
        String path = "$..messages[?(@.code==\"{}\")].message";

        if(topic.equalsIgnoreCase(propertyConfiguration.getSavePropertyTopic()))
            path = path.replace("{}",PTConstants.NOTIFICATION_CREATE_CODE);

        if(topic.equalsIgnoreCase(propertyConfiguration.getUpdatePropertyTopic()) && userType.equalsIgnoreCase("CITIZEN"))
            path = path.replace("{}",PTConstants.NOTIFICATION_UPDATE_CODE);

        if(topic.equalsIgnoreCase(propertyConfiguration.getUpdatePropertyTopic()) && !userType.equalsIgnoreCase("CITIZEN"))
            path = path.replace("{}",PTConstants.NOTIFICATION_EMPLOYEE_UPDATE_CODE);

        return path;
    }

    /**
     * Returns the message for each specific property
     * @param property The property for which the notification has to be sent
     * @param message The standard message format
     * @return The customized message for the given property
     */
    private String getCustomizedCreateMessage(Property property, String message){
        message = message.replace("<insert ID>",property.getPropertyId());
        if(property.getAddress().getDoorNo()!=null)
            message = message.replace("<House No.>",property.getAddress().getDoorNo());
        else
            message = message.replace("<House No.>,","");

        if(property.getAddress().getBuildingName()!=null)
            message = message.replace("<Colony Name>",property.getAddress().getBuildingName());
        else
            message = message.replace("<Colony Name>,","");

        if(property.getAddress().getStreet()!=null)
            message = message.replace("<Street No.>",property.getAddress().getStreet());
        else
            message = message.replace("<Street No.>,","");

        if(property.getAddress().getLocality().getCode()!=null)
            message = message.replace("<Mohalla>",property.getAddress().getLocality().getName());
        else
            message = message.replace("<Mohalla>,","");

        if(property.getAddress().getCity()!=null)
            message = message.replace("<City>",property.getAddress().getCity());
        else
            message = message.replace("<City>.","");

        if(property.getAddress().getPincode()!=null)
            message = message.replace("<Pincode>",property.getAddress().getPincode());
        else
            message = message.replace("<Pincode>","");

        return message;
    }


    /**
     *  Returns customized message for update done by citizen
     * @param property Property which is updated
     * @param message Standard message template for update by citizen
     * @return Customized message for update by citizen
     */
    private String getCustomizedUpdateMessage(Property property,String message){
        PropertyDetail propertyDetail = property.getPropertyDetails().get(0);
        message = message.replace("<insert ID>",property.getPropertyId());
        message = message.replace("<FY>",propertyDetail.getFinancialYear());
        message = message.replace("<insert no>",propertyDetail.getAssessmentNumber());
       return message;
    }


    /**
     *  Returns customized message for update done by employee
     * @param property Property which is updated
     * @param message Standard message template for update by employee
     * @return Customized message for update by employee
     */
    private String getCustomizedUpdateMessageEmployee(Property property,String message){
        PropertyDetail propertyDetail = property.getPropertyDetails().get(0);
        message = message.replace("<insert Property Tax Assessment ID>",property.getPropertyId());
        message = message.replace("<FY>",propertyDetail.getFinancialYear());
      //message = message.replace("<insert inactive URL for Citizen Web application>.",notificationURL);
        return message;
    }

    /**
     * Get all the unique mobileNumbers of the owners of the property
     * @param property The property whose unique mobileNumber are to be returned
     * @return Unique mobileNumber of the given property
     */
    private Set<String> getMobileNumbers(Property property){
        Set<String> mobileNumbers = new HashSet<>();
        property.getPropertyDetails().forEach(propertyDetail -> {
            propertyDetail.getOwners().forEach(owner -> {
                mobileNumbers.add(owner.getMobileNumber());
            });
        });
        return mobileNumbers;
    }


    /**
     * Creates SMSRequest for the given mobileNumber with the given message
     * @param mobileNumbers The set of mobileNumber for which SMSRequest has to be created
     * @param customizedMessage The message to sent
     * @return List of SMSRequest
     */
    private List<SMSRequest> getSMSRequests(Set<String> mobileNumbers,String customizedMessage){
        List<SMSRequest> smsRequests = new ArrayList<>();
             mobileNumbers.forEach(mobileNumber-> {
                 if(mobileNumber!=null){
                     SMSRequest smsRequest = new SMSRequest(mobileNumber,customizedMessage);
                     smsRequests.add(smsRequest);
                 }
             });
         return smsRequests;
    }

    /**
     * Send the SMSRequest on the SMSNotification kafka topic
     * @param smsRequestList The list of SMSRequest to be sent
     */
    private void sendSMS(List<SMSRequest> smsRequestList){
        if (propertyConfiguration.getIsSMSNotificationEnabled()) {
            if (CollectionUtils.isEmpty(smsRequestList))
                log.info("Messages from localization couldn't be fetched!");
            for(SMSRequest smsRequest: smsRequestList) {
                producer.push(propertyConfiguration.getSmsNotifTopic(), smsRequest);
            }
        }
    }




}

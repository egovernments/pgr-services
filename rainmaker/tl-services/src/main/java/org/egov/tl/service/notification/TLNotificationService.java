package org.egov.tl.service.notification;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tl.config.TLConfiguration;
import org.egov.tl.producer.Producer;
import org.egov.tl.repository.ServiceRequestRepository;
import org.egov.tl.util.NotificationUtil;
import org.egov.tl.web.models.SMSRequest;
import org.egov.tl.web.models.TradeLicense;
import org.egov.tl.web.models.TradeLicenseRequest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;


@Slf4j
@Service
public class TLNotificationService {


    private TLConfiguration config;

    private ServiceRequestRepository serviceRequestRepository;

    private NotificationUtil util;


    @Autowired
    public TLNotificationService(TLConfiguration config, ServiceRequestRepository serviceRequestRepository, NotificationUtil util) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
        this.util = util;
    }

    /**
     * Creates and send the sms based on the tradeLicenseRequest
     * @param request The tradeLicenseRequest listenend on the kafka topic
     */
    public void process(TradeLicenseRequest request){
        List<SMSRequest> smsRequests = new LinkedList<>();
        enrichSMSRequest(request,smsRequests);
        util.sendSMS(smsRequests);
    }


    /**
     * Enriches the smsRequest with the customized messages
     * @param request The tradeLicenseRequest from kafka topic
     * @param smsRequests List of SMSRequets
     */
    private void enrichSMSRequest(TradeLicenseRequest request,List<SMSRequest> smsRequests){
        String tenantId = request.getLicenses().get(0).getTenantId();
        String localizationMessages = util.getLocalizationMessages(tenantId,request.getRequestInfo());
        for(TradeLicense license : request.getLicenses()){
            String message = util.getCustomizedMsg(request.getRequestInfo(),license,localizationMessages);
            if(message==null) continue;

            Map<String,String > mobileNumberToOwner = new HashMap<>();

            license.getTradeLicenseDetail().getOwners().forEach(owner -> {
                if(owner.getMobileNumber()!=null)
                    mobileNumberToOwner.put(owner.getMobileNumber(),owner.getName());
            });
            smsRequests.addAll(createSMSRequest(message,mobileNumberToOwner));
        }
    }


    /**
     * Creates sms request for the each owners
     * @param message The message for the specific tradeLicense
     * @param mobileNumberToOwnerName Map of mobileNumber to OwnerName
     * @return List of SMSRequest
     */
    private List<SMSRequest> createSMSRequest(String message,Map<String,String> mobileNumberToOwnerName){
        List<SMSRequest> smsRequest = new LinkedList<>();
        for(Map.Entry<String,String> entryset : mobileNumberToOwnerName.entrySet()) {
            String customizedMsg = message.replace("<1>",entryset.getValue());
            smsRequest.add(new SMSRequest(entryset.getKey(),customizedMsg));
        }
            return smsRequest;
    }




}

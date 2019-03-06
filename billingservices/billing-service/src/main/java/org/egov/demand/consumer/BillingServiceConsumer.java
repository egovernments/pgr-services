package org.egov.demand.consumer;

import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.config.ApplicationProperties;
import org.egov.demand.helper.CollectionReceiptRequest;
import org.egov.demand.model.DemandUpdateMisRequest;
import org.egov.demand.model.enums.Status;
import org.egov.demand.repository.BillRepository;
import org.egov.demand.service.BusinessServDetailService;
import org.egov.demand.service.DemandAdjustmentService;
import org.egov.demand.service.DemandService;
import org.egov.demand.service.GlCodeMasterService;
import org.egov.demand.service.TaxHeadMasterService;
import org.egov.demand.service.TaxPeriodService;
import org.egov.demand.web.contract.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BillingServiceConsumer {


	@Autowired
	private ApplicationProperties applicationProperties;

	@Autowired
	private DemandService demandService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private BillRepository billRepository;

	@Autowired
	private TaxHeadMasterService taxheadMasterService;

	@Autowired
	private TaxPeriodService taxPeriodService;
	
	@Autowired
	private GlCodeMasterService glCodeMasterService;

	@Autowired
	private BusinessServDetailService businessServDetailService;
	
	@Autowired
	private DemandAdjustmentService demandAdjustmentService;


	@KafkaListener(topics = { "${kafka.topics.receipt.update.collecteReceipt}","${kafka.topics.updateMIS.demand}","${kafka.topics.save.bill}", "${kafka.topics.update.bill}", "${kafka.topics.save.demand}",
			"${kafka.topics.update.demand}" , "${kafka.topics.save.taxHeadMaster}","${kafka.topics.update.taxHeadMaster}",
			"${kafka.topics.create.taxperiod.name}", "${kafka.topics.update.taxperiod.name}","${kafka.topics.save.glCodeMaster}",
			"${kafka.topics.update.glCodeMaster}","${kafka.topics.receipt.update.demand}",
			"${kafka.topics.create.businessservicedetail.name}", "${kafka.topics.update.businessservicedetail.name}", "${kafka.topics.receipt.cancel.name}"})
	public void processMessage(Map<String, Object> consumerRecord, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
		log.debug("key:" + topic + ":" + "value:" + consumerRecord);
		
		try {

			if (applicationProperties.getCreateDemandTopic().equals(topic))
				demandService.save(objectMapper.convertValue(consumerRecord, DemandRequest.class));
			else if (applicationProperties.getUpdateDemandTopic().equals(topic))
				demandService.update(objectMapper.convertValue(consumerRecord, DemandRequest.class));
			/*else if(applicationProperties.getUpdateDemandFromReceipt().equals(topic))
				demandService.updateDemandFromBill(objectMapper.convertValue(consumerRecord, BillRequest.class));
			*/else if (topic.equals(applicationProperties.getCreateBillTopic()))
				billRepository.saveBill(objectMapper.convertValue(consumerRecord, BillRequest.class));
			else if (applicationProperties.getCreateTaxHeadMasterTopicName().equals(topic))
				taxheadMasterService.create(objectMapper.convertValue(consumerRecord, TaxHeadMasterRequest.class));
			else if (applicationProperties.getUpdateTaxHeadMasterTopicName().equals(topic))
				taxheadMasterService.update(objectMapper.convertValue(consumerRecord, TaxHeadMasterRequest.class));
			else if(applicationProperties.getCreateTaxPeriodTopicName().equals(topic))
				taxPeriodService.create(objectMapper.convertValue(consumerRecord, TaxPeriodRequest.class));
			else if(applicationProperties.getUpdateTaxPeriodTopicName().equals(topic))
				taxPeriodService.update(objectMapper.convertValue(consumerRecord, TaxPeriodRequest.class));
			else if(applicationProperties.getCreateGlCodeMasterTopicName().equals(topic))
				glCodeMasterService.create(objectMapper.convertValue(consumerRecord, GlCodeMasterRequest.class));
			else if(applicationProperties.getCreateBusinessServiceDetailTopicName().equals(topic))
				businessServDetailService.create(objectMapper.convertValue(consumerRecord, BusinessServiceDetailRequest.class));
			else if(applicationProperties.getUpdateBusinessServiceDetailTopicName().equals(topic))
				businessServDetailService.update(objectMapper.convertValue(consumerRecord, BusinessServiceDetailRequest.class));
			else if(applicationProperties.getUpdateGlCodeMasterTopicName().equals(topic))
				glCodeMasterService.update(objectMapper.convertValue(consumerRecord, GlCodeMasterRequest.class));
			else if(applicationProperties.getUpdateMISTopicName().equals(topic))
				demandService.updateMIS(objectMapper.convertValue(consumerRecord, DemandUpdateMisRequest.class));
			else if(applicationProperties.getUpdateDemandFromReceipt().equals(topic)){
				CollectionReceiptRequest collectionReceiptRequest = objectMapper.convertValue(consumerRecord, CollectionReceiptRequest.class);
				RequestInfo requestInfo = collectionReceiptRequest.getRequestInfo();
				List<Receipt> receipts = collectionReceiptRequest.getReceipt();
				
				ReceiptRequest receiptRequest = ReceiptRequest.builder()
						.receipt(receipts).requestInfo(requestInfo)
						.tenantId(collectionReceiptRequest.getTenantId()).build();
				log.debug("the receipt request is -------------------"+receiptRequest);
				//FIXME TODO bill update commented out 
				//demandService.updateDemandFromReceipt(receiptRequest,Status.CREATED);
			}
			else if(applicationProperties.getSaveCollectedReceipts().equals(topic))
				demandService.saveCollectedReceipts(objectMapper.convertValue(consumerRecord, BillRequest.class));
			
			else if(applicationProperties.getReceiptCancellationTopic().equals(topic)) {
				demandAdjustmentService.adjustDemandOnReceiptCancellation(objectMapper.convertValue(consumerRecord, ReceiptRequest.class));
			}
			
		} catch (Exception exception) {
			log.debug("processMessage:" + exception);
			throw exception;
		}
	}
}

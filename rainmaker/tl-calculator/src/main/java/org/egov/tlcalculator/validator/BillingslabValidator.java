package org.egov.tlcalculator.validator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.tlcalculator.service.BillingslabService;
import org.egov.tlcalculator.utils.BillingslabConstants;
import org.egov.tlcalculator.utils.ErrorConstants;
import org.egov.tlcalculator.web.models.BillingSlab;
import org.egov.tlcalculator.web.models.BillingSlabReq;
import org.egov.tlcalculator.web.models.BillingSlabRes;
import org.egov.tlcalculator.web.models.BillingSlabSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BillingslabValidator {
		
	@Autowired
	private BillingslabService service;
	
	
	public void validateCreate(BillingSlabReq billingSlabReq) {
		Map<String, String> errorMap = new HashMap<>();
		tenantIdCheck(billingSlabReq, errorMap);
		duplicateCheck(billingSlabReq, errorMap);
		Map<String, List<String>> mdmsDataMap = service.getMDMSDataForValidation(billingSlabReq);
		billingSlabReq.getBillingSlab().parallelStream().forEach(slab -> validateMDMSCodes(slab, mdmsDataMap, errorMap));
		if(!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}
	}
	
	public void validateUpdate(BillingSlabReq billingSlabReq) {
		Map<String, String> errorMap = new HashMap<>();
		tenantIdCheck(billingSlabReq, errorMap);
		areRecordsExisiting(billingSlabReq, errorMap);
		Map<String, List<String>> mdmsDataMap = service.getMDMSDataForValidation(billingSlabReq);
		billingSlabReq.getBillingSlab().parallelStream().forEach(slab -> validateMDMSCodes(slab, mdmsDataMap, errorMap));
		if(!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}
	}
	
	public void tenantIdCheck(BillingSlabReq billingSlabReq, Map<String, String> errorMap) {
		Set<String> tenantIds = billingSlabReq.getBillingSlab().parallelStream().map(BillingSlab::getTenantId).collect(Collectors.toSet());
		if(tenantIds.size() > 1) {
			errorMap.put(ErrorConstants.MULTIPLE_TENANT_CODE, ErrorConstants.MULTIPLE_TENANT_MSG);
			throw new CustomException(errorMap);
		}
	}
	
	public void duplicateCheck(BillingSlabReq billingSlabReq, Map<String, String> errorMap) {
		List<BillingSlab> duplicateSlabs = new ArrayList<>();
		billingSlabReq.getBillingSlab().parallelStream().forEach(slab -> {
			BillingSlabSearchCriteria criteria = BillingSlabSearchCriteria.builder().tenantId(slab.getTenantId()).accessoryCategory(slab.getAccessoryCategory())
					.tradeType(slab.getTradeType()).licenseType(slab.getLicenseType().toString()).structureType(slab.getStructureType()).uom(slab.getUom())
					.type(slab.getType().toString()).from(slab.getFromUom()).to(slab.getToUom()).build();
			BillingSlabRes slabRes = service.searchSlabs(criteria, billingSlabReq.getRequestInfo());
			if(!CollectionUtils.isEmpty(slabRes.getBillingSlab())) {
				duplicateSlabs.add(slab);
			}
		});
		if(!CollectionUtils.isEmpty(duplicateSlabs)) {
			errorMap.put(ErrorConstants.DUPLICATE_SLABS_CODE, ErrorConstants.DUPLICATE_SLABS_MSG + ": "+duplicateSlabs);
			throw new CustomException(errorMap);	
		}
	}
	
	public void areRecordsExisiting(BillingSlabReq billingSlabReq, Map<String, String> errorMap) {
		BillingSlabSearchCriteria criteria = BillingSlabSearchCriteria.builder().tenantId(billingSlabReq.getBillingSlab().get(0).getTenantId())
				.ids(billingSlabReq.getBillingSlab().parallelStream().map(BillingSlab :: getId).collect(Collectors.toList())).build();
		BillingSlabRes slabRes = service.searchSlabs(criteria, billingSlabReq.getRequestInfo());
		List<String> ids = new ArrayList<>();
		if(billingSlabReq.getBillingSlab().size() != slabRes.getBillingSlab().size()) {
			List<String> responseIds = slabRes.getBillingSlab().parallelStream().map(BillingSlab :: getId).collect(Collectors.toList());
			for(BillingSlab slab: billingSlabReq.getBillingSlab()) {
				if(!responseIds.contains(slab.getId()))
					ids.add(slab.getId());
			}
			errorMap.put(ErrorConstants.INVALID_IDS_CODE, ErrorConstants.INVALID_IDS_MSG + ": "+ids);
			throw new CustomException(errorMap);
		}
		
	}
	public void validateMDMSCodes(BillingSlab billingSlab, Map<String, List<String>> mdmsDataMap, Map<String, String> errorMap) {
		if(!StringUtils.isEmpty(billingSlab.getTradeType())) {
			if(!mdmsDataMap.get(BillingslabConstants.TL_MDMS_TRADETYPE).contains(billingSlab.getTradeType()))
				errorMap.put(ErrorConstants.INVALID_TRADETYPE_CODE, ErrorConstants.INVALID_TRADETYPE_MSG + ": "+billingSlab.getTradeType());
		}
		if(!StringUtils.isEmpty(billingSlab.getAccessoryCategory())) {
			if(!mdmsDataMap.get(BillingslabConstants.TL_MDMS_ACCESSORIESCATEGORY).contains(billingSlab.getAccessoryCategory()))
				errorMap.put(ErrorConstants.INVALID_ACCESSORIESCATEGORY_CODE, ErrorConstants.INVALID_ACCESSORIESCATEGORY_MSG + ": "+billingSlab.getAccessoryCategory());
		}
		if(!StringUtils.isEmpty(billingSlab.getStructureType())) {
			if(!mdmsDataMap.get(BillingslabConstants.TL_MDMS_STRUCTURETYPE).contains(billingSlab.getStructureType()))
				errorMap.put(ErrorConstants.INVALID_STRUCTURETYPE_CODE, ErrorConstants.INVALID_STRUCTURETYPE_MSG + ": "+billingSlab.getStructureType());
		}
		if(!StringUtils.isEmpty(billingSlab.getUom())) {
			if(!mdmsDataMap.get(BillingslabConstants.TL_MDMS_UOM).contains(billingSlab.getUom()))
				errorMap.put(ErrorConstants.INVALID_UOM_CODE, ErrorConstants.INVALID_UOM_MSG + ": "+billingSlab.getUom());
		}
	}

}
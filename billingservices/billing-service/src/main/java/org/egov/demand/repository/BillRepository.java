package org.egov.demand.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.demand.model.Bill;
import org.egov.demand.model.BillAccountDetail;
import org.egov.demand.model.BillDetail;
import org.egov.demand.model.BillSearchCriteria;
import org.egov.demand.model.BusinessServiceDetail;
import org.egov.demand.repository.querybuilder.BillQueryBuilder;
import org.egov.demand.repository.rowmapper.SearchBillRowMapper;
import org.egov.demand.web.contract.BillRequest;
import org.egov.demand.web.contract.BillResponse;
import org.egov.demand.web.contract.BusinessServiceDetailCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class BillRepository {

	@Autowired
	private BillQueryBuilder billQueryBuilder;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private SearchBillRowMapper searchBillRowMapper;
	
	@Autowired
	private BusinessServiceDetailRepository businessServiceDetailRepository;
	
	public List<Bill> findBill(BillSearchCriteria billCriteria){
		
		List<Object> preparedStatementValues = new ArrayList<>();
		String queryStr = billQueryBuilder.getBillQuery(billCriteria, preparedStatementValues);
		log.debug("query:::"+queryStr+"  preparedStatementValues::"+preparedStatementValues);
		
		return jdbcTemplate.query(queryStr, preparedStatementValues.toArray(), searchBillRowMapper);
	}
	
	@Transactional
	public void saveBill(BillRequest billRequest){
		
		RequestInfo requestInfo = billRequest.getRequestInfo();
		List<Bill> bills = billRequest.getBills();
		
		log.debug("saveBill requestInfo:"+requestInfo);
		log.debug("saveBill bills:"+bills);
		jdbcTemplate.batchUpdate(billQueryBuilder.INSERT_BILL_QUERY, new BatchPreparedStatementSetter() {
			
			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				Bill bill = bills.get(index);

				ps.setString(1, bill.getId());
				ps.setString(2, bill.getTenantId());
				ps.setString(3, bill.getPayerName());
				ps.setString(4, bill.getPayerAddress());
				ps.setString(5, bill.getPayerEmail());
				ps.setBoolean(6, bill.getIsActive());
				ps.setBoolean(7, bill.getIsCancelled());
				ps.setString(8, requestInfo.getUserInfo().getId().toString());
				ps.setLong(9, new Date().getTime());
				ps.setString(10, requestInfo.getUserInfo().getId().toString());
				ps.setLong(11, new Date().getTime());
			}
			
			@Override
			public int getBatchSize() {
				return bills.size();
			}
		});
		saveBillDetails(billRequest);
	}
	
	public void saveBillDetails(BillRequest billRequest){
		
		RequestInfo requestInfo = billRequest.getRequestInfo();
		List<Bill> bills = billRequest.getBills();
		List<BillDetail> billDetails = new ArrayList<>();
		List<BillAccountDetail> billAccountDetails = new ArrayList<>();
		for(Bill bill:bills){
			List<BillDetail> tempBillDetails  = bill.getBillDetails();
			billDetails.addAll(tempBillDetails);
			
			for(BillDetail billDetail : tempBillDetails){
				billAccountDetails.addAll(billDetail.getBillAccountDetails());
			}
		}
		log.debug("saveBillDeails tempBillDetails:"+billDetails);
		jdbcTemplate.batchUpdate(billQueryBuilder.INSERT_BILLDETAILS_QUERY, new BatchPreparedStatementSetter() {
				
			
			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				BillDetail billDetail = billDetails.get(index);

				ps.setString(1, billDetail.getId());
				ps.setString(2, billDetail.getTenantId());
				ps.setString(3, billDetail.getBill());
				ps.setString(4, billDetail.getBusinessService());
				ps.setString(5, billDetail.getBillNumber());
				ps.setLong(6, billDetail.getBillDate());
				ps.setString(7, billDetail.getConsumerCode());
				ps.setString(8, billDetail.getConsumerType());
				ps.setString(9, null);
				ps.setString(10, null);
				ps.setObject(11, billDetail.getMinimumAmount());
				ps.setObject(12, billDetail.getTotalAmount());
				// apportioning logic does not reside in billing service anymore 
				ps.setBoolean(13, false);
				ps.setBoolean(14, billDetail.getPartPaymentAllowed());
				List<String> collectionModesNotAllowed = billDetail.getCollectionModesNotAllowed();
				if (collectionModesNotAllowed.isEmpty())
					ps.setString(15, null);
				else
					ps.setString(15, StringUtils.join(billDetail.getCollectionModesNotAllowed(), ","));
				ps.setString(16, requestInfo.getUserInfo().getId().toString());
				ps.setLong(17, new Date().getTime());
				ps.setString(18, requestInfo.getUserInfo().getId().toString());
				ps.setLong(19, new Date().getTime());
			}
				
			@Override
			public int getBatchSize() {
				return billDetails.size();
			}
		});
		saveBillAccountDetail(billAccountDetails, requestInfo);
	}
	
	public void saveBillAccountDetail(List<BillAccountDetail> billAccountDetails, RequestInfo requestInfo){
		log.debug("saveBillAccountDetail billAccountDetails:"+billAccountDetails);
		//final List<BillDetail> billDetails = tempBillDetails;	
		jdbcTemplate.batchUpdate(billQueryBuilder.INSERT_BILLACCOUNTDETAILS_QUERY, new BatchPreparedStatementSetter() {
				
			@Override
			public void setValues(PreparedStatement ps, int index) throws SQLException {
				BillAccountDetail billAccountDetail = billAccountDetails.get(index);
				
				String purpose = null;
				if(billAccountDetail.getPurpose()!=null)
					purpose = billAccountDetail.getPurpose().toString();
					
				ps.setString(1, billAccountDetail.getId());
				ps.setString(2, billAccountDetail.getTenantId());
				ps.setString(3, billAccountDetail.getBillDetail());
				ps.setString(4, billAccountDetail.getGlcode());
				ps.setObject(5, billAccountDetail.getOrder());
				ps.setString(6, null);
				ps.setBigDecimal(7,billAccountDetail.getAmount());
				ps.setObject(8, billAccountDetail.getAdjustedAmount());
				ps.setObject(9, null);
				ps.setObject(10, billAccountDetail.getIsActualDemand());
				ps.setString(11, purpose);
				ps.setString(12, requestInfo.getUserInfo().getId().toString());
				ps.setLong(13, new Date().getTime());
				ps.setString(14, requestInfo.getUserInfo().getId().toString());
				ps.setLong(15, new Date().getTime());
			}
				
			@Override
			public int getBatchSize() {
				return billAccountDetails.size();
			}
		});
	}

	@Deprecated
	public List<Bill> apportion(BillRequest billRequest) {

		RequestInfo requestInfo = billRequest.getRequestInfo();
		List<Bill> inputBills = billRequest.getBills();
		List<Bill> reqBills = null;
		BillRequest inputBillrequest = new BillRequest();
		Map<String, BillDetail> responseBillDetailsMap = new HashMap<>();
		inputBillrequest.setRequestInfo(requestInfo);

		BusinessServiceDetailCriteria businessCriteria = BusinessServiceDetailCriteria.builder()
				.tenantId(inputBills.get(0).getTenantId()).build();
		businessServiceDetailRepository.searchBusinessServiceDetails(businessCriteria);
		Map<String, BusinessServiceDetail> businessServicesMap = businessServiceDetailRepository
				.searchBusinessServiceDetails(businessCriteria).stream()
				.filter(BusinessServiceDetail::getCallBackForApportioning)
				.collect(Collectors.toMap(BusinessServiceDetail::getBusinessService, Function.identity()));

		for (String businessService : businessServicesMap.keySet()) {

			String url = businessServicesMap.get(businessService).getCallBackApportionURL();
			reqBills = new ArrayList<>();
			for (Bill bill : inputBills) {

				Bill reqBill = bill;
				List<BillDetail> reqbillDetails = new ArrayList<>();
				for (BillDetail billDetail : reqBill.getBillDetails())
					if (billDetail.getBusinessService().equalsIgnoreCase(businessService))
						reqbillDetails.add(billDetail);
				reqBill.setBillDetails(reqbillDetails);
				reqBills.add(reqBill);
			}
			inputBillrequest.setBills(reqBills);
			List<Bill> responseBills = restTemplate.postForObject("http://pt-calculator-v2:8080"+url, billRequest, BillResponse.class).getBill();
			for (Bill bill : responseBills) {
				for (BillDetail detail : bill.getBillDetails())
					responseBillDetailsMap.put(detail.getId(), detail);
			}
		}
		return getApportionedBillList(inputBills, responseBillDetailsMap);
	}

	private List<Bill> getApportionedBillList(List<Bill> inputBills, Map<String, BillDetail> responseBillDetailsMap) {

		for (Bill bill : inputBills) {
			List<BillDetail> billDetails = new ArrayList<>();
			for (BillDetail detail : bill.getBillDetails()) {

				String id = detail.getId();
				if (id != null)
					billDetails.add(responseBillDetailsMap.get(id));
				else
					billDetails.add(detail);
			}
			bill.setBillDetails(billDetails);
		}
		return inputBills;
	}
}


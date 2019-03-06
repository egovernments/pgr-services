package org.egov.demand.repository.rowmapper;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.demand.model.AuditDetail;
import org.egov.demand.model.AuditDetails;
import org.egov.demand.model.Bill;
import org.egov.demand.model.BillAccountDetail;
import org.egov.demand.model.BillAccountDetail.PurposeEnum;
import org.egov.demand.model.BillDetail;
import org.egov.demand.model.GlCodeMaster;
import org.egov.demand.model.TaxHeadMaster;
import org.egov.demand.model.enums.Category;
import org.egov.demand.model.enums.Purpose;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SearchBillRowMapper implements ResultSetExtractor<List<Bill>>{

	@Override
	public List<Bill> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, Bill> billMap = new HashMap<>();
		Map<String, BillDetail> billDetailMap = new HashMap<>();

		while (rs.next()) {
			try {
				String billId = rs.getString("b_id");
				Bill bill = billMap.get(billId);
	
				if (bill == null) {
					bill = new Bill();
					bill.setId(billId);
					bill.setTenantId(rs.getString("b_tenantid"));
					bill.setPayerName(rs.getString("b_payeename"));
					bill.setPayerAddress(rs.getString("b_payeeaddress"));
					bill.setPayerEmail(rs.getString("b_payeeemail"));
					bill.setIsActive(rs.getBoolean("b_isactive"));
					bill.setIsCancelled(rs.getBoolean("b_iscancelled"));
	
					AuditDetails auditDetails = new AuditDetails();
					auditDetails.setCreatedBy(rs.getString("b_createdby"));
					auditDetails.setCreatedTime((Long)rs.getObject("b_createddate"));
					auditDetails.setLastModifiedBy(rs.getString("b_lastmodifiedby"));
					auditDetails.setLastModifiedTime((Long)rs.getObject("b_lastmodifieddate"));
					
					bill.setAuditDetails(auditDetails);
					
					billMap.put(bill.getId(), bill);
				}

				String detailId = rs.getString("bd_id");
				BillDetail billDetail = billDetailMap.get(detailId);
				
				if(billDetail == null){
					billDetail = new BillDetail();
					billDetail.setId(detailId);
					billDetail.setTenantId(rs.getString("bd_tenantid"));
					billDetail.setBill(rs.getString("bd_billid"));
					billDetail.setBusinessService(rs.getString("bd_businessservice"));
					billDetail.setBillNumber(rs.getString("bd_billno"));
					billDetail.setBillDate(rs.getLong("bd_billdate"));
					billDetail.setConsumerCode(rs.getString("bd_consumercode"));
					billDetail.setConsumerType(rs.getString("bd_consumertype"));
					//billDetail.setBillDescription(rs.getString("bd_billdescription"));
					//billDetail.setDisplayMessage(rs.getString("bd_displaymessage"));
					billDetail.setMinimumAmount(rs.getBigDecimal("bd_minimumamount"));
					billDetail.setTotalAmount(rs.getBigDecimal("bd_totalamount"));
					//billDetail.setCallBackForApportioning(rs.getBoolean("bd_callbackforapportioning"));
					billDetail.setPartPaymentAllowed(rs.getBoolean("bd_partpaymentallowed"));
					billDetail.setCollectionModesNotAllowed(Arrays.asList(rs.getString("bd_collectionmodesnotallowed").split(",")));

					billDetailMap.put(billDetail.getId(), billDetail);

					if (bill.getId().equals(billDetail.getBill()))
						bill.getBillDetails().add(billDetail);
				}

				BillAccountDetail billAccDetail=new BillAccountDetail();
				billAccDetail.setId(rs.getString("ad_id"));
				billAccDetail.setTenantId(rs.getString("ad_tenantid"));
				billAccDetail.setBillDetail(rs.getString("ad_billdetail"));
				billAccDetail.setGlcode(rs.getString("ad_glcode"));
				billAccDetail.setOrder(rs.getInt("ad_orderno"));
				//billAccDetail.setAccountDescription(rs.getString("ad_accountdescription"));
				billAccDetail.setAdjustedAmount(rs.getBigDecimal("ad_creditamount"));
				//billAccDetail.setDebitAmount(rs.getBigDecimal("ad_debitamount"));
				billAccDetail.setIsActualDemand(rs.getBoolean("ad_isactualdemand"));
				billAccDetail.setPurpose(PurposeEnum.fromValue(rs.getString("ad_purpose")));
				billAccDetail.setAmount(rs.getBigDecimal("ad_cramounttobepaid"));
				
				if(billDetail.getId().equals(billAccDetail.getBillDetail()))
					billDetail.getBillAccountDetails().add(billAccDetail);
				
			} catch (Exception e) {
				log.debug("exception in taxHeadMasterRowMapper : " + e);
				throw new RuntimeException("error while mapping object from reult set : " + e);
			}
		}
		log.debug("converting map to list object ::: " + billMap.values());
		return new ArrayList<>(billMap.values());
	}

}

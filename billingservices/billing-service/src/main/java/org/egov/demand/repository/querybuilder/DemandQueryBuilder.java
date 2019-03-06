/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.demand.repository.querybuilder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.egov.demand.model.DemandCriteria;
import org.egov.demand.model.DemandDetailCriteria;
import org.egov.demand.model.DemandUpdateMisRequest;
import org.egov.demand.model.enums.Type;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DemandQueryBuilder {

	public static final String BASE_DEMAND_QUERY = "SELECT dmd.id AS did,dmd.consumercode AS dconsumercode,"
			+ "dmd.consumertype AS dconsumertype,dmd.businessservice AS dbusinessservice,dmd.payer,"
			+ "dmd.taxperiodfrom AS dtaxperiodfrom,dmd.taxperiodto AS dtaxperiodto,"
			+ "dmd.minimumamountpayable AS dminimumamountpayable,dmd.createdby AS dcreatedby,"
			+ "dmd.lastmodifiedby AS dlastmodifiedby,dmd.createdtime AS dcreatedtime,"
			+ "dmd.lastmodifiedtime AS dlastmodifiedtime,dmd.tenantid AS dtenantid,dmd.status,dmd.additionaldetails as demandadditionaldetails,"

			+ "dmdl.id AS dlid,dmdl.demandid AS dldemandid,dmdl.taxheadcode AS dltaxheadcode,"
			+ "dmdl.taxamount AS dltaxamount,dmdl.collectionamount AS dlcollectionamount,"
			+ "dmdl.createdby AS dlcreatedby,dmdl.lastModifiedby AS dllastModifiedby,"
			+ "dmdl.createdtime AS dlcreatedtime,dmdl.lastModifiedtime AS dllastModifiedtime,"
			+ "dmdl.tenantid AS dltenantid,dmdl.additionaldetails as detailadditionaldetails " + "FROM egbs_demand dmd "
			+ "INNER JOIN egbs_demanddetail dmdl ON dmd.id=dmdl.demandid " + "AND dmd.tenantid=dmdl.tenantid WHERE ";

	public static final String BASE_DEMAND_DETAIL_QUERY = "SELECT "
			+ "demanddetail.id AS dlid,demanddetail.demandid AS dldemandid,demanddetail.taxheadcode AS dltaxheadcode,"
			+ "demanddetail.taxamount AS dltaxamount,demanddetail.collectionamount AS dlcollectionamount,"
			+ "demanddetail.createdby AS dlcreatedby,demanddetail.lastModifiedby AS dllastModifiedby,"
			+ "demanddetail.createdtime AS dlcreatedtime,demanddetail.lastModifiedtime AS dllastModifiedtime,"
			+ "demanddetail.tenantid AS dltenantid " + " FROM egbs_demanddetail demanddetail "
					+ "INNER JOIN egbs_demand demand ON demanddetail.demandid=demand.id AND "
					+ "demanddetail.tenantid=demand.tenantid WHERE ";

	public static final String DEMAND_QUERY_ORDER_BY_CLAUSE = "demand.taxperiodfrom";

	public static final String BASE_DEMAND_DETAIL_QUERY_ORDER_BY_CLAUSE = "demanddetail.id";

	public static final String DEMAND_INSERT_QUERY = "INSERT INTO egbs_demand "
			+ "(id,consumerCode,consumerType,businessService,owner,taxPeriodFrom,taxPeriodTo,"
			+ "minimumAmountPayable,createdby,lastModifiedby,createdtime,lastModifiedtime,tenantid, status, additionaldetails) "
			+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

	public static final String DEMAND_DETAIL_INSERT_QUERY = "INSERT INTO egbs_demanddetail "
			+ "(id,demandid,taxHeadCode,taxamount,collectionamount,"
			+ "createdby,lastModifiedby,createdtime,lastModifiedtime,tenantid,additionaldetails)" 
			+ " VALUES (?,?,?,?,?,?,?,?,?,?,?);";

	// FIX ME REMOVE CREATED BY FROM UPDATE
	public static final String DEMAND_UPDATE_QUERY = "UPDATE egbs_demand SET "
			+ "id=?,consumerCode=?,consumerType=?,businessService=?,owner=?,taxPeriodFrom=?,"
			+ "taxPeriodTo=?,minimumAmountPayable=?,lastModifiedby=?," + "lastModifiedtime=?,tenantid=?, status=?"
			+ ", additionaldetails=? WHERE id=? AND tenantid=?;";

	public static final String DEMAND_DETAIL_UPDATE_QUERY = "UPDATE egbs_demanddetail SET "
			+ "id=?,demandid=?,taxHeadCode=?,taxamount=?,collectionamount=?,"
			+ "lastModifiedby=?,lastModifiedtime=?, additionaldetails=? WHERE id=? AND tenantid=?;";
	
	public final String DEMAND_UPDATE_CONSUMERCODE_QUERY="UPDATE egbs_demand SET consumercode=?, lastmodifiedby=?, lastmodifiedtime=? "
			+ " WHERE tenantid=? AND id IN (";
	
	public static final String COLLECTED_RECEIPT_INSERT_QUERY="INSERT INTO egbs_collectedreceipts(id, businessservice, consumercode, receiptnumber,"
			+ " receiptamount, receiptdate, status, tenantid, createdby, createddate, lastmodifiedby, lastmodifieddate)"
			+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	
	public  final String Collected_Receipt_search_Query="select * from egbs_collectedreceipts where ";
	
	public String getCollectedReceiptsQuery(DemandCriteria demandCriteria){
		StringBuilder query = new StringBuilder(Collected_Receipt_search_Query);
		query.append(" tenantid = '" + demandCriteria.getTenantId() + "' AND businessservice='"
				+ demandCriteria.getBusinessService() + "' AND consumercode IN ("
				+ getIdQueryForStrings(demandCriteria.getConsumerCode())+" order by consumercode desc");
		log.info("query::"+query);
		return query.toString();
	}
	public String getDemandQueryForConsumerCodes(Map<String,Set<String>> businessConsumercodeMap,List<Object> preparedStmtList, String tenantId){
		
		StringBuilder query = new StringBuilder(BASE_DEMAND_QUERY);
		
		query.append("demand.tenantid=? ");
		preparedStmtList.add(tenantId);
		boolean orFlag = false;
		for (Entry<String, Set<String>> consumerCode : businessConsumercodeMap.entrySet()) {
			
			String businessService = consumerCode.getKey();
			Set<String> consumerCodes = consumerCode.getValue();
			
			if(consumerCodes!=null && !consumerCodes.isEmpty()){
				
				if(orFlag)
					query.append("OR");
				else
					query.append("AND");
				
				query.append(" demand.businessservice='"+businessService+"' AND demand.consumercode IN ("
						+getIdQueryForStrings(consumerCodes));
				orFlag=true;
			}
		}
		
		return query.toString();
				}

	public String getDemandQuery(DemandCriteria demandCriteria,Set<String> ownerIds, List<Object> preparedStatementValues) {

		StringBuilder demandQuery = new StringBuilder(BASE_DEMAND_QUERY);

		demandQuery.append("demand.tenantid=?");
		preparedStatementValues.add(demandCriteria.getTenantId());

		if (demandCriteria.getDemandId() != null && !demandCriteria.getDemandId().isEmpty()) {
			addAndClause(demandQuery);
			demandQuery.append("demand.id IN (" + getIdQueryForStrings(demandCriteria.getDemandId()));
		}
		if (ownerIds != null && !ownerIds.isEmpty()) {
			addAndClause(demandQuery);
			demandQuery.append("demand.owner IN (" + getIdQueryForStrings(ownerIds));
		}
		if (demandCriteria.getBusinessService() != null) {
			addAndClause(demandQuery);
			demandQuery.append("demand.businessservice=?");
			preparedStatementValues.add(demandCriteria.getBusinessService());
		}
		if (demandCriteria.getConsumerCode() != null && !demandCriteria.getConsumerCode().isEmpty()) {
			addAndClause(demandQuery);
			demandQuery.append("demand.consumercode IN ("
			+ getIdQueryForStrings(demandCriteria.getConsumerCode()));
		}
		if (demandCriteria.getDemandFrom() != null) {
			addAndClause(demandQuery);
			demandQuery.append("demanddetail.taxamount>=?");
			preparedStatementValues.add(demandCriteria.getDemandFrom());
		}
		if (demandCriteria.getDemandTo() != null) {
			addAndClause(demandQuery);
			demandQuery.append("demanddetail.taxamount<=?");
			preparedStatementValues.add(demandCriteria.getDemandTo());
		}
		if (demandCriteria.getType() != null) {
			if (demandCriteria.getType().equals(Type.CURRENT)) {
				addAndClause(demandQuery);
				Long currDate = new Date().getTime();
				demandQuery.append("demand.taxperiodfrom<=?");
				preparedStatementValues.add(currDate);
				addAndClause(demandQuery);
				demandQuery.append("demand.taxperiodto>=?");
				preparedStatementValues.add(currDate);
				
			} else if (demandCriteria.getType().equals(Type.ARREARS)) {
				addAndClause(demandQuery);
				demandQuery.append("demand.taxperiodto<?");
				Long currDate = new Date().getTime();
				preparedStatementValues.add(currDate);
			}
		}
		addOrderByClause(demandQuery, DEMAND_QUERY_ORDER_BY_CLAUSE);
		addPagingClause(demandQuery, preparedStatementValues);

		log.info("the query String for demand : " + demandQuery.toString());
		return demandQuery.toString();
	}
	
	public static String getDemandDetailQuery(DemandDetailCriteria demandDetailCriteria,
			List<Object> preparedStatementValues) {

		StringBuilder demandDetailQuery = new StringBuilder(BASE_DEMAND_DETAIL_QUERY);

		demandDetailQuery.append("demanddetail.tenantid=?");
		preparedStatementValues.add(demandDetailCriteria.getTenantId());

		if (demandDetailCriteria.getDemandId() != null) {
			addAndClause(demandDetailQuery);
			demandDetailQuery.append("demanddetail.demandid=?");
			preparedStatementValues.add(demandDetailCriteria.getDemandId());
		}
		if (demandDetailCriteria.getTaxHeadCode() != null) {
			addAndClause(demandDetailQuery);
			demandDetailQuery.append("demanddetail.taxheadcode=?");
			preparedStatementValues.add(demandDetailCriteria.getTaxHeadCode());
		}
		if (demandDetailCriteria.getPeriodFrom() != null) {
			addAndClause(demandDetailQuery);
			demandDetailQuery.append("demand.taxPeriodFrom=?");
			preparedStatementValues.add(demandDetailCriteria.getPeriodFrom());
		}
		if(demandDetailCriteria.getPeriodTo() != null){
			addAndClause(demandDetailQuery);
			demandDetailQuery.append("demand.taxPeriodTo=?");
			preparedStatementValues.add(demandDetailCriteria.getPeriodTo());
		}
		if(demandDetailCriteria.getDetailsId() !=null && 
				!demandDetailCriteria.getDetailsId().isEmpty()){
			addAndClause(demandDetailQuery);
			demandDetailQuery.append("demanddetail.id IN (" +getIdQueryForStrings(demandDetailCriteria.getDetailsId()));
		}
		addOrderByClause(demandDetailQuery, BASE_DEMAND_DETAIL_QUERY_ORDER_BY_CLAUSE);
		addPagingClause(demandDetailQuery, preparedStatementValues);
		log.info("the query String for demand detail: " + demandDetailQuery.toString());
		return demandDetailQuery.toString();
	}
	//query builder for update mis(updating consumer code)
	public String getDemandUpdateMisQuery(DemandUpdateMisRequest demandRequest){

		return DEMAND_UPDATE_CONSUMERCODE_QUERY+getIdQueryForStrings(demandRequest.getId());
	}

	private static void addOrderByClause(StringBuilder demandQueryBuilder,String columnName) {
		demandQueryBuilder.append(" ORDER BY " + columnName);
	}

	private static void addPagingClause(StringBuilder demandQueryBuilder, List<Object> preparedStatementValues) {
		demandQueryBuilder.append(" LIMIT ?");
		preparedStatementValues.add(500);
		demandQueryBuilder.append(" OFFSET ?");
		preparedStatementValues.add(0);
	}

	private static boolean addAndClause(StringBuilder queryString) {
		queryString.append(" AND ");
		return true;
	}
	
	private static String getIdQueryForStrings(Set<String> idList) {

		StringBuilder query = new StringBuilder();
		if (!idList.isEmpty()) {

			String[] list = idList.toArray(new String[idList.size()]);
			query.append("'"+list[0]+"'");
			for (int i = 1; i < idList.size(); i++) {
				query.append("," + "'"+list[i]+"'");
			}
		}
		return query.append(")").toString();
	}
}

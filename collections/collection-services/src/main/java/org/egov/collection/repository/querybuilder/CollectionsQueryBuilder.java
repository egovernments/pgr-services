package org.egov.collection.repository.querybuilder;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.egov.collection.model.AuditDetails;
import org.egov.collection.model.Instrument;
import org.egov.collection.model.ReceiptSearchCriteria;
import org.egov.collection.web.contract.Bill;
import org.egov.collection.web.contract.BillAccountDetail;
import org.egov.collection.web.contract.BillDetail;
import org.egov.collection.web.contract.Receipt;
import org.egov.tracer.model.CustomException;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toSet;

public class CollectionsQueryBuilder {

    public static final String INSERT_RECEIPT_HEADER_SQL = "INSERT INTO egcl_receiptheader(id, payeename, payeeaddress, payeeemail, payeemobile, paidby, referencenumber, "
            + " receipttype, receiptnumber, receiptdate, businessdetails, collectiontype, reasonforcancellation, minimumamount, totalamount, "
            + " collectedamount, collmodesnotallwd, consumercode, channel,boundary, voucherheader, "
            + "depositedbranch, createdby, createddate, lastmodifiedby, lastmodifieddate, tenantid, referencedate, referencedesc, "
            + " manualreceiptdate, manualreceiptnumber, reference_ch_id, stateid, location, isreconciled, "
            + "status, transactionid, fund, function, department, additionalDetails) "
            + "VALUES (:id, :payeename, :payeeaddress, :payeeemail, :payeemobile , :paidby, :referencenumber, :receipttype, "
            + ":receiptnumber, :receiptdate, :businessdetails, :collectiontype, :reasonforcancellation, :minimumamount, :totalamount, "
            + " :collectedamount, :collmodesnotallwd, :consumercode, :channel, :boundary, :voucherheader, "
            + ":depositedbranch, :createdby, :createddate, :lastmodifiedby, :lastmodifieddate, :tenantid, :referencedate, :referencedesc, "
            + " :manualreceiptdate, :manualreceiptnumber, :reference_ch_id, :stateid, :location, :isreconciled, "
            + ":status, :transactionid, :fund, :function, :department, :additionalDetails )";

    public static final String INSERT_RECEIPT_DETAILS_SQL = "INSERT INTO egcl_receiptdetails(id, chartofaccount, dramount, cramount, ordernumber, receiptheader, actualcramounttobepaid, "
            + "description, financialyear, isactualdemand, purpose, tenantid, additionalDetails) "
            + "VALUES (:id, :chartofaccount, :dramount, :cramount, :ordernumber, :receiptheader, " +
            ":actualcramounttobepaid, "
            + ":description, :financialyear, :isactualdemand, :purpose, :tenantid, :additionalDetails)";

    public static final String INSERT_INSTRUMENT_HEADER_SQL = "INSERT INTO egcl_instrumentheader(id, transactionnumber, transactiondate, amount, instrumenttype, "
            +
            "instrumentstatus, bankid, branchname, bankaccountid, ifsccode, financialstatus, transactiontype, payee, drawer, surrenderreason, serialno, createdby,"
            +
            " createddate, lastmodifiedby, lastmodifieddate, tenantid, additionalDetails, instrumentDate, instrumentNumber)\n" +
            " VALUES " +
            " (:id, :transactionnumber, :transactiondate, :amount, :instrumenttype, :instrumentstatus, :bankid, " +
            ":branchname, :bankaccountid, :ifsccode, :financialstatus, :transactiontype, :payee, :drawer, " +
            ":surrenderreason, :serialno, :createdby, :createddate, :lastmodifiedby, :lastmodifieddate, :tenantid, " +
            ":additionalDetails, :instrumentDate, :instrumentNumber) ";

    public static final String INSERT_INSTRUMENT_SQL = "insert into egcl_receiptinstrument(instrumentheader, " +
            "receiptheader) values (:instrumentheader, :receiptheader)";

    public static final String UPDATE_INSTRUMENT_STATUS_SQL = "UPDATE egcl_instrumentheader SET instrumentstatus = :instrumentstatus" +
            " , additionalDetails = :additionalDetails ,lastmodifiedby = :lastmodifiedby , lastmodifieddate =  " +
            ":lastmodifieddate WHERE id = :id " ;

    public static final String UPDATE_RECEIPT_STATUS_SQL = "UPDATE egcl_receiptheader SET status = :status , " +
            "reasonforcancellation = :reasonforcancellation , voucherheader = :voucherheader, additionalDetails = " +
            ":additionalDetails ," +
            "lastmodifiedby = :lastmodifiedby , lastmodifieddate =  :lastmodifieddate WHERE id = :id " ;

    private static final String SELECT_RECEIPTS_SQL = "Select rh.id as rh_id,rh.payeename as rh_payeename,rh" +
            ".payeeAddress as rh_payeeAddress, rh.payeeEmail as rh_payeeEmail, rh.payeemobile as rh_payeemobile, rh" +
            ".paidBy as rh_paidBy, rh.referenceNumber as rh_referenceNumber, rh.referenceDate as rh_referenceDate,rh.receiptType as "
            +
            "rh_receiptType, rh.receiptNumber as rh_receiptNumber, rh.receiptDate as rh_receiptDate, rh.referenceDesc" +
            " as rh_referenceDesc, rh.manualReceiptNumber as rh_manualReceiptNumber, rh.fund as rh_fund, rh.function as rh_function, rh.department as rh_department,  rh.manualreceiptdate as "
            +
            "rh_manualreceiptdate, rh.businessDetails as rh_businessDetails,  rh.collectionType as rh_collectionType,rh.stateId as rh_stateId,rh.location as "
            +
            "rh_location,  rh.isReconciled as rh_isReconciled,rh.status as rh_status,rh.reasonForCancellation as " +
            "rh_reasonForCancellation , rh.minimumAmount as rh_minimumAmount,rh.totalAmount as rh_totalAmount, rh" +
            ".collectedamount as rh_collectedamount, rh.collModesNotAllwd as rh_collModesNotAllwd,rh.consumerCode as " +
            "rh_consumerCode,rh.function as rh_function,  rh.version as rh_version,rh.channel as rh_channel,rh.reference_ch_id as "
            +
            "rh_reference_ch_id,  rh.consumerType as rh_consumerType,rh.fund as rh_fund,rh.fundSource as " +
            "rh_fundSource, rh.boundary as rh_boundary, rh.department as rh_department,rh.depositedBranch as " +
            "rh_depositedBranch, rh.tenantId as rh_tenantId, rh.displayMsg as rh_displayMsg,rh.voucherheader as " +
            "rh_voucherheader, rh.cancellationRemarks as rh_cancellationRemarks, rh.additionalDetails as " +
            "rh_additionalDetails, rh.createdBy as  rh_createdBy, rh.createdDate as rh_createdDate,rh.lastModifiedBy " +
            "as rh_lastModifiedBy, rh.lastModifiedDate as " +
            "rh_lastModifiedDate, rh.transactionid as rh_transactionid, rd.id as rd_id,  rd.amount as rd_amount, " +
            "rd.adjustedamount as rd_adjustedamount, rd.ordernumber as " +
            "rd_ordernumber,  rd.description as rd_description,rd.chartOfAccount as rd_chartOfAccount,rd" +
            ".isActualDemand  as rd_isActualDemand,  rd.financialYear as rd_financialYear,rd.purpose as rd_purpose, " +
            "rd.additionalDetails as rd_additionalDetails, rd.tenantId as rd_tenantId,   ins.id as ins_instrumentheader, " +
            "ins.amount as ins_amount, ins.transactionDate as ins_transactiondate, ins.transactionNumber as " +
            "ins_transactionNumber, ins.instrumenttype as ins_instrumenttype, ins .instrumentstatus" +
            " as ins_instrumentstatus,  ins.bankid as ins_bankid , ins.branchname as ins_branchname , ins" +
            ".bankaccountid as ins_bankaccountid,  ins.ifsccode as ins_ifsccode , ins.financialstatus as " +
            "ins_financialstatus ,  ins.transactiontype as ins_transactiontype , ins.payee as ins_payee , ins.drawer " +
            "as ins_drawer ,  ins.surrenderreason as ins_surrenderreason , ins.serialno as ins_serialno , ins" +
            ".additionalDetails as ins_additionalDetails, ins.createdby as ins_createdby ,  ins.createddate as ins_createddate , ins.lastmodifiedby as "
            +
            "ins_lastmodifiedby ,  ins.lastmodifieddate as ins_lastmodifieddate , ins.tenantid as ins_tenantid , " +
            " ins.instrumentDate as ins_instrumentDate, ins.instrumentNumber as ins_instrumentNumber " +
            "from egcl_receiptheader rh LEFT OUTER JOIN egcl_receiptdetails rd ON rh.id=rd.receiptheader " +
            "LEFT OUTER JOIN egcl_receiptinstrument recins ON rh.id=recins.receiptheader " +
            "LEFT JOIN egcl_instrumentheader ins ON recins.instrumentheader=ins.id ";

    private static final String PAGINATION_WRAPPER = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY rh_id) offset_ FROM " +
            "({baseQuery})" +
            " result) result_offset " +
            "WHERE offset_ > :offset AND offset_ <= :limit";

    public static final String UPDATE_RECEIPT_HEADER_SQL = "UPDATE egcl_receiptheader SET paidby = :paidby, " +
            "payeeaddress = :payeeaddress, payeeemail = :payeeemail, payeename = :payeename, manualreceiptnumber = :manualreceiptnumber," +
            " manualreceiptdate = :manualreceiptdate, status = :status, voucherheader = :voucherheader, additionalDetails = :additionalDetails," +
            " lastmodifiedby = :lastmodifiedby, lastmodifieddate = :lastmodifieddate" +
            " where id=:id";

    public static final String UPDATE_INSTRUMENT_HEADER_SQL = "UPDATE egcl_instrumentheader SET instrumentstatus = :instrumentstatus, " +
            " payee = :payee, lastmodifiedby = :lastmodifiedby, lastmodifieddate = :lastmodifieddate, additionalDetails = :additionalDetails"+
            " where id=:id";


    public static MapSqlParameterSource getParametersForReceiptHeader(Receipt receipt, BillDetail billDetail) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        AuditDetails auditDetails = receipt.getAuditDetails();
        Bill bill = receipt.getBill().get(0);

        String collectionModesNotAllowed = String.join(",", billDetail.getCollectionModesNotAllowed());

        sqlParameterSource.addValue("id", billDetail.getId());
        sqlParameterSource.addValue("payeename", bill.getPayerName());
        sqlParameterSource.addValue("payeeaddress", bill.getPayerAddress());
        sqlParameterSource.addValue("payeeemail", bill.getPayerEmail());
        sqlParameterSource.addValue("payeemobile", bill.getMobileNumber());
        sqlParameterSource.addValue("paidby", bill.getPaidBy());
        sqlParameterSource.addValue("referencenumber", billDetail.getBillNumber());
        sqlParameterSource.addValue("receipttype", billDetail.getReceiptType());
        sqlParameterSource.addValue("receiptnumber", billDetail.getReceiptNumber());
        sqlParameterSource.addValue("receiptdate", billDetail.getReceiptDate());
        sqlParameterSource.addValue("businessdetails", billDetail.getBusinessService());
        sqlParameterSource.addValue("collectiontype", billDetail.getCollectionType().toString());
        sqlParameterSource.addValue("reasonforcancellation", billDetail.getReasonForCancellation());
        sqlParameterSource.addValue("minimumamount", billDetail.getMinimumAmount());
        sqlParameterSource.addValue("totalamount", billDetail.getTotalAmount());
        sqlParameterSource.addValue("collectedamount", billDetail.getCollectedAmount());
        sqlParameterSource.addValue("collmodesnotallwd", collectionModesNotAllowed);
        sqlParameterSource.addValue("consumercode", billDetail.getConsumerCode());
        sqlParameterSource.addValue("channel", billDetail.getChannel());
        sqlParameterSource.addValue("boundary", billDetail.getBoundary());
        sqlParameterSource.addValue("voucherheader", billDetail.getVoucherHeader());
        sqlParameterSource.addValue("depositedbranch", null);
        sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
        sqlParameterSource.addValue("createddate", auditDetails.getCreatedDate());
        sqlParameterSource.addValue("lastmodifiedby", auditDetails.getLastModifiedBy());
        sqlParameterSource.addValue("lastmodifieddate", auditDetails.getLastModifiedDate());
        sqlParameterSource.addValue("tenantid", billDetail.getTenantId());
        sqlParameterSource.addValue("referencedate", billDetail.getBillDate());
        sqlParameterSource.addValue("manualreceiptnumber", billDetail.getManualReceiptNumber());
        sqlParameterSource.addValue("manualreceiptdate", billDetail.getManualReceiptDate());
        sqlParameterSource.addValue("reference_ch_id", null);
        sqlParameterSource.addValue("stateid", null);
        sqlParameterSource.addValue("location", null);
        sqlParameterSource.addValue("isreconciled", false);
        sqlParameterSource.addValue("status", billDetail.getStatus());
        sqlParameterSource.addValue("transactionid", receipt.getInstrument().getTransactionNumber());
        sqlParameterSource.addValue("fund", billDetail.getFund());
        sqlParameterSource.addValue("function", billDetail.getFunction());
        sqlParameterSource.addValue("department", billDetail.getDepartment());
        sqlParameterSource.addValue("additionalDetails", getJsonb(billDetail.getAdditionalDetails()));

        return sqlParameterSource;

    }

    public static MapSqlParameterSource getParametersForReceiptHeaderUpdate(Receipt receipt, BillDetail billDetail) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        Bill bill = receipt.getBill().get(0);

        sqlParameterSource.addValue("id", billDetail.getId());
        sqlParameterSource.addValue("paidby", bill.getPaidBy());
        sqlParameterSource.addValue("payeeaddress", bill.getPayerAddress());
        sqlParameterSource.addValue("payeeemail", bill.getPayerEmail());
        sqlParameterSource.addValue("payeename", bill.getPayerName());
        sqlParameterSource.addValue("manualreceiptnumber", billDetail.getManualReceiptNumber());
        sqlParameterSource.addValue("manualreceiptdate", billDetail.getManualReceiptDate());
        sqlParameterSource.addValue("additionalDetails", getJsonb(billDetail.getAdditionalDetails()));
        sqlParameterSource.addValue("lastmodifiedby", receipt.getAuditDetails().getLastModifiedBy());
        sqlParameterSource.addValue("lastmodifieddate", receipt.getAuditDetails().getLastModifiedDate());

        // Temporary code below, to enable backward compatibility with previous API
        sqlParameterSource.addValue("voucherheader", billDetail.getVoucherHeader());
        sqlParameterSource.addValue("status", billDetail.getStatus());


        return sqlParameterSource;

    }

    public static MapSqlParameterSource getParametersForInstrumentHeaderUpdate(Instrument instrument,
                                                                          AuditDetails auditDetails) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("id", instrument.getId());
        sqlParameterSource.addValue("payee", instrument.getPayee());
        sqlParameterSource.addValue("lastmodifiedby", auditDetails.getLastModifiedBy());
        sqlParameterSource.addValue("lastmodifieddate", auditDetails.getLastModifiedDate());
        sqlParameterSource.addValue("additionalDetails", getJsonb(instrument.getAdditionalDetails()));

        // Temporary code below, to enable backward compatibility with previous API
        sqlParameterSource.addValue("instrumentstatus", instrument.getInstrumentStatus().toString());

        return sqlParameterSource;
    }

    public static MapSqlParameterSource getParametersForReceiptDetails(BillAccountDetail billAccountDetails,
            String receiptHeaderId) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("id", billAccountDetails.getId());
        sqlParameterSource.addValue("chartofaccount", billAccountDetails.getGlcode());
        sqlParameterSource.addValue("ordernumber", billAccountDetails.getOrder());
        sqlParameterSource.addValue("financialyear", null);
        sqlParameterSource.addValue("isactualdemand", billAccountDetails.getIsActualDemand());
        sqlParameterSource.addValue("purpose", billAccountDetails.getPurpose().toString());
        sqlParameterSource.addValue("tenantid", billAccountDetails.getTenantId());
        sqlParameterSource.addValue("receiptheader", receiptHeaderId);
        sqlParameterSource.addValue("amount", billAccountDetails.getAmount());
        sqlParameterSource.addValue("adjustedAmount", billAccountDetails.getAdjustedAmount());
        sqlParameterSource.addValue("additionalDetails", getJsonb(billAccountDetails.getAdditionalDetails()));

        return sqlParameterSource;

    }

    public static MapSqlParameterSource getParametersForInstrumentHeader(Instrument instrument, AuditDetails auditDetails) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("id", instrument.getId());
        sqlParameterSource.addValue("transactionnumber", instrument.getTransactionNumber());
        sqlParameterSource.addValue("transactiondate", instrument.getTransactionDateInput());
        sqlParameterSource.addValue("amount", instrument.getAmount());
        sqlParameterSource.addValue("instrumenttype", instrument.getInstrumentType().getName());
        sqlParameterSource.addValue("instrumentstatus", instrument.getInstrumentStatus().toString());
        sqlParameterSource.addValue("bankid", isNull(instrument.getBank()) ? null : instrument.getBank().getName());
        sqlParameterSource.addValue("branchname", instrument.getBranchName());
        sqlParameterSource.addValue("bankaccountid", isNull(instrument.getBankAccount()) ? null : instrument
                .getBankAccount().getAccountNumber());
        sqlParameterSource.addValue("ifsccode", instrument.getIfscCode());
        sqlParameterSource.addValue("financialstatus", null);
        sqlParameterSource.addValue("transactiontype", instrument.getTransactionType().toString());
        sqlParameterSource.addValue("payee", instrument.getPayee());
        sqlParameterSource.addValue("drawer", instrument.getDrawer());
        sqlParameterSource.addValue("surrenderreason", instrument.getSurrenderReason());
        sqlParameterSource.addValue("serialno", instrument.getSerialNo());
        sqlParameterSource.addValue("createdby", auditDetails.getCreatedBy());
        sqlParameterSource.addValue("createddate", auditDetails.getCreatedDate());
        sqlParameterSource.addValue("lastmodifiedby", auditDetails.getLastModifiedBy());
        sqlParameterSource.addValue("lastmodifieddate", auditDetails.getLastModifiedDate());
        sqlParameterSource.addValue("tenantid", instrument.getTenantId());
        sqlParameterSource.addValue("additionalDetails", getJsonb(instrument.getAdditionalDetails()));
        sqlParameterSource.addValue("instrumentNumber", instrument.getInstrumentNumber());
        sqlParameterSource.addValue("instrumentDate", instrument.getInstrumentDate());

        return sqlParameterSource;

    }

    public static MapSqlParameterSource getParametersForReceiptStatusUpdate(BillDetail billDetail, AuditDetails
            auditDetails){
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("id", billDetail.getId());
        sqlParameterSource.addValue("status", billDetail.getStatus());
        sqlParameterSource.addValue("voucherheader", billDetail.getVoucherHeader());
        sqlParameterSource.addValue("reasonforcancellation", billDetail.getReasonForCancellation());
        sqlParameterSource.addValue("additionalDetails", getJsonb(billDetail.getAdditionalDetails()));
        sqlParameterSource.addValue("lastmodifiedby", auditDetails.getLastModifiedBy());
        sqlParameterSource.addValue("lastmodifieddate", auditDetails.getLastModifiedDate());

        return sqlParameterSource;
    }

    public static MapSqlParameterSource getParametersForInstrumentStatusUpdate(Instrument instrument, AuditDetails
            auditDetails){
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("instrumentstatus", instrument.getInstrumentStatus().toString());
        sqlParameterSource.addValue("id", instrument.getId());
        sqlParameterSource.addValue("additionalDetails", getJsonb(instrument.getAdditionalDetails()));
        sqlParameterSource.addValue("lastmodifiedby", auditDetails.getLastModifiedBy());
        sqlParameterSource.addValue("lastmodifieddate", auditDetails.getLastModifiedDate());

        return sqlParameterSource;
    }

    public static MapSqlParameterSource getParametersForInstrument(Instrument instrument, String receiptHeaderId) {
        MapSqlParameterSource sqlParameterSource = new MapSqlParameterSource();
        sqlParameterSource.addValue("instrumentheader", instrument.getId());
        sqlParameterSource.addValue("receiptheader", receiptHeaderId);
        return sqlParameterSource;
    }

    @SuppressWarnings("rawtypes")
    public static String getReceiptSearchQuery(ReceiptSearchCriteria searchCriteria,
            Map<String, Object> preparedStatementValues) {
        StringBuilder selectQuery = new StringBuilder(SELECT_RECEIPTS_SQL);

        addWhereClause(selectQuery, preparedStatementValues, searchCriteria);

        return addPaginationClause(selectQuery, preparedStatementValues, searchCriteria);
    }

    @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
    private static void addWhereClause(StringBuilder selectQuery, Map<String, Object> preparedStatementValues,
            ReceiptSearchCriteria searchCriteria) {

        if (StringUtils.isNotBlank(searchCriteria.getTenantId())) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.tenantId =:tenantId");
            preparedStatementValues.put("tenantId", searchCriteria.getTenantId());

        }

        if (searchCriteria.getReceiptNumbers() != null && !searchCriteria.getReceiptNumbers().isEmpty()) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.receiptNumber IN (:receiptNumbers)  ");
            preparedStatementValues.put("receiptNumbers", searchCriteria.getReceiptNumbers());
        }

        if (!isNull(searchCriteria.getConsumerCode()) && !searchCriteria.getConsumerCode().isEmpty()) {

            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.consumerCode in (:consumerCodes)");
            preparedStatementValues.put("consumerCodes", searchCriteria.getConsumerCode());
        }

        if (!isNull(searchCriteria.getStatus()) && !searchCriteria.getStatus().isEmpty()) {

            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" UPPER(rh.status) in (:status)");
            preparedStatementValues.put("status",
                    searchCriteria.getStatus()
                        .stream()
                        .map(String::toUpperCase)
                        .collect(toSet())
            );
        }

        if (!isNull(searchCriteria.getInstrumentType()) && !searchCriteria.getInstrumentType().isEmpty()) {

            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" UPPER(ins.instrumenttype) in (:instrumenttype)");
            preparedStatementValues.put("instrumenttype",
                    searchCriteria.getInstrumentType()
                            .stream()
                            .map(String::toUpperCase)
                            .collect(toSet())
            );
        }

        if (StringUtils.isNotBlank(searchCriteria.getFund())) {

            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.fund = :fund");
            preparedStatementValues.put("fund", searchCriteria.getFund());
        }

        if (StringUtils.isNotBlank(searchCriteria.getDepartment())) {

            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.department = :department");
            preparedStatementValues.put("department", searchCriteria.getDepartment());
        }

        if (StringUtils.isNotBlank(searchCriteria.getCollectedBy())) {

            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.createdBy = :collectedBy");
            preparedStatementValues.put("collectedBy", new Long(searchCriteria
                    .getCollectedBy()));
        }

        if (searchCriteria.getFromDate() != null) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.receiptDate >= :fromDate");
            preparedStatementValues.put("fromDate", searchCriteria.getFromDate());
        }

        if (searchCriteria.getToDate() != null) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.receiptDate <= :toDate");
            Calendar c = Calendar.getInstance();
            c.setTime(new Date(searchCriteria.getToDate()));
            c.add(Calendar.DATE, 1);
            searchCriteria.setToDate(c.getTime().getTime());

            preparedStatementValues.put("toDate", searchCriteria.getToDate());
        }

        if (StringUtils.isNotBlank(searchCriteria.getBusinessCode())) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.businessDetails = :businessCode");
            preparedStatementValues.put("businessCode", searchCriteria.getBusinessCode());
        }

        if (searchCriteria.getBusinessCodes() != null && !searchCriteria.getBusinessCodes().isEmpty()) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.businessDetails IN (:businessCodes)  ");
            preparedStatementValues.put("businessCodes", searchCriteria.getBusinessCodes());
        }

        if (searchCriteria.getIds() != null) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.id IN (:ids)");
            preparedStatementValues.put("ids", searchCriteria.getIds());
        }

        if (StringUtils.isNotBlank(searchCriteria.getTransactionId())) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.transactionid = :transactionId ");
            preparedStatementValues.put("transactionId", searchCriteria.getTransactionId());
        }

        if (searchCriteria.getManualReceiptNumbers() != null && !searchCriteria.getManualReceiptNumbers().isEmpty()) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.manualreceiptnumber IN (:manualReceiptNumbers) ");
            preparedStatementValues.put("manualReceiptNumbers", searchCriteria.getManualReceiptNumbers());
        }

        if (searchCriteria.getBillIds() != null && !searchCriteria.getBillIds().isEmpty()) {
            addClauseIfRequired(preparedStatementValues, selectQuery);
            selectQuery.append(" rh.referencenumber IN (:billIds) ");
            preparedStatementValues.put("billIds", searchCriteria.getBillIds());
        }
    }

    private static void addClauseIfRequired(Map<String, Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else {
            queryString.append(" AND");
        }
    }

    private static String addOrderByClause(String selectQuery,
            ReceiptSearchCriteria criteria) {
//        String sortBy = (criteria.getSortBy() == null ? "receiptDate" : "rh." + criteria.getSortBy());
//        String sortOrder = (criteria.getSortOrder() == null ? "DESC" : criteria
//                .getSortOrder());
        return selectQuery + " ORDER BY rh_receiptDate DESC ";
    }

    private static String addPaginationClause(StringBuilder selectQuery, Map<String, Object> preparedStatementValues,
            ReceiptSearchCriteria criteria) {

        if (criteria.getLimit()!=null && criteria.getLimit() != 0) {
            String finalQuery = PAGINATION_WRAPPER.replace("{baseQuery}", selectQuery);
            preparedStatementValues.put("offset", criteria.getOffset());
            preparedStatementValues.put("limit", criteria.getOffset() + criteria.getLimit());

            return addOrderByClause(finalQuery, criteria);

        } else
            return addOrderByClause(selectQuery.toString(), criteria);
    }

    private static PGobject getJsonb(JsonNode node) {
        if (Objects.isNull(node))
            return null;

        PGobject pgObject = new PGobject();
        pgObject.setType("jsonb");
        try {
            pgObject.setValue(node.toString());
            return pgObject;
        } catch (SQLException e) {
            throw new CustomException("UNABLE_TO_CREATE_RECEIPT", "Invalid JSONB value provided");
        }

    }

}

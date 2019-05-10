package org.egov.receipt.consumer.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Instrument{

	/*
	 * id is the unique reference to Instrument Header entered in the system.
	 */
	private String id;

	/*
	 * transactionNumber unique number of the instrument. For cheque type this
	 * is cheque date. For DD type it is DD number
	 *
	 */
	private String transactionNumber;

	/*
	 * transactionDate is the date of instrument . For cheque type it is cheque
	 * date. for DD it is DD date
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	private Date transactionDate;

    /**
     * Transaction date as long comes from UI in case of cheque and DD
     */
    private Long transactionDateInput;

	/*
	 * amount is the instrument amount. For cheque type it is cheque amount.
	 */
	@NotNull
	@Min(value = 0)
	@Max(value = 999999999)
	private BigDecimal amount;

	/*
	 * instrumentType specifies the type of the instrument - The folowing are
	 * the different types Cash,Cheque,DD,POC
	 *
	 */
	@NotNull	
	private InstrumentType instrumentType;
	
	private Long instrumentDate;
	
	private String instrumentNumber;

	/*
	 * bank references to the bank from which the payment/Receipt is made.
	 */
	private BankContract bank;

	/*
	 * branchName is the branch name entered in the collection Receipt.
	 */

	@Size(max = 50)
	private String branchName;

	/*
	 * bankAccount is the reference of the Bank account from which the payment
	 * instrument is assigned
	 */
	private BankAccountContract bankAccount;

	/**
	 * IFSC Code of the bank branch
	 */
	private String ifscCode;


	/*
	 * transactionType are of two kinds -Debit and Credit. When its a receipt
	 * instrument it is Debit and in case of payment instrument its credit.
	 */
	private TransactionType transactionType;

	/**
	 * Status of the instrument, newly added
	 */
	private InstrumentStatusEnum instrumentStatus;

	/*
	 * payee is the entity who is making the payment via instrument
	 */
	@Size(max = 50)
	private String payee;

	/*
	 * drawer is the entity to which the payment is made.
	 */
	@Size(max = 100)
	private String drawer;

	/*
	 * surrenderReason is the reason from the defined list seleted while
	 * surrendering a payment cheque. Depending on the reason, the cheque can be
	 * re-used or not is decided.
	 */
	private SurrenderReason surrenderReason;

	/*
	 * serialNo is the series of the cheque numbers from which the instrument is
	 * assigned from. The cheque numbers in an account is defined based on Year,
	 * Bank account and tagged to a department.
	 */
	//@NotBlank
	@Size(max = 50, min = 2)
	private String serialNo;

	/*
	 * instrumentVouchers is the reference to the payment vouchers for which the
	 * instrument is attached.
	 */
	// @DrillDownTable
	private Set<InstrumentVoucher> instrumentVouchers = new HashSet<InstrumentVoucher>(0);

    private AuditDetails auditDetails;

    private JsonNode additionalDetails;

    @NotNull
	private String tenantId;
    
    public InstrumentContract toContract() {

        InstrumentContract contract = new InstrumentContract();

        contract.setId(this.getId());
        contract.setAmount(this.getAmount());
        contract.setBank(this.getBank());
        contract.setBankAccount(this.getBankAccount());
        contract.setBranchName(this.getBranchName());
        contract.setDrawer(this.getDrawer());
        contract.setInstrumentType(this.getInstrumentType());
        contract.setSurrenderReason(this.getSurrenderReason());
        if (this.getInstrumentVouchers() != null) {

            List<InstrumentVoucherContract> instrumentVouchers = new ArrayList<>();

            if (this.getInstrumentVouchers() != null)
                for (InstrumentVoucher iv : this.getInstrumentVouchers())
                    instrumentVouchers
                            .add(InstrumentVoucherContract.builder().instrument(contract.getId())
                                    .voucherHeaderId(iv.getVoucherHeaderId()).build());

            contract.setInstrumentVouchers(instrumentVouchers);

        }
        contract.setPayee(this.getPayee());
        contract.setSerialNo(this.getSerialNo());
        contract.setTransactionDate(this.getTransactionDate());
        contract.setTransactionNumber(this.getTransactionNumber());
        contract.setTransactionType(this.getTransactionType());
        contract.setTenantId(this.getTenantId());
        return contract;
    }

}


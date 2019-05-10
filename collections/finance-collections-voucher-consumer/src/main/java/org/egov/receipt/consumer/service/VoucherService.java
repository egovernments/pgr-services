package org.egov.receipt.consumer.service;

import org.egov.receipt.consumer.model.ReceiptReq;
import org.egov.receipt.consumer.model.VoucherResponse;

public interface VoucherService {
	public VoucherResponse createReceiptVoucher(ReceiptReq req) throws Exception;
	public VoucherResponse cancelReceiptVoucher(ReceiptReq req) throws Exception;
	public boolean isVoucherCreationEnabled(ReceiptReq req) throws Exception;
	public boolean isVoucherExists(ReceiptReq receiptRequest) throws Exception;
}

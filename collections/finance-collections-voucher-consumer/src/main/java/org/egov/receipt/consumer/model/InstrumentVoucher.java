package org.egov.receipt.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class InstrumentVoucher {

	/*
	 * instrumentHeaderId is the reference of the instrument attached to a
	 * voucher
	 */
	private Instrument instrument;

	/*
	 * voucherHeaderId is the reference of the voucher attached to a instrument.
	 */
	private String voucherHeaderId;

}

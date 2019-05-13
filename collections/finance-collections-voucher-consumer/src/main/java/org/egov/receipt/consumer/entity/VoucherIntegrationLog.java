package org.egov.receipt.consumer.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name="egf_voucher_integration_log")
public class VoucherIntegrationLog {
@Id
@SequenceGenerator(name="seq-gen",sequenceName="seq_egf_voucher_integration_log", initialValue=1)
@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="seq-gen")
int id;
@Column(name="referenceNumber")
String referenceNumber;
@Column(name="status")
String status;
@Column(name="voucherNumber")
String voucherNumber;
@Column(name="[type]")
String type;
@Column(name="requestJson")
String requestJson;
@Column(name="description")
String description;
}

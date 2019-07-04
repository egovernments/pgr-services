package org.egov.receipt.consumer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OnlineGLCodeMapping {
private String servicecode;
private String glcode;
private Long validFrom;
private Long validTo;
}

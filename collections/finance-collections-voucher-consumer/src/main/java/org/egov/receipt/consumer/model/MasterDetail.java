package org.egov.receipt.consumer.model;

import java.io.Serializable;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
@Component
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MasterDetail implements Serializable{
    private String name;
    private String filter;
}

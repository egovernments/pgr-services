package org.egov.wcms.web.models;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * what type of billing is applicable for particular connection, supported values are \"code\" from \"BillingType\" Master.
 */
public enum BillingType {
  
  METERBASED("MeterBased"),
  
  PLOTBASED("PlotBased"),
  
  TABBASED("TabBased"),
  
  FIXED("Fixed"),
  
  CUSTOM("Custom");

  private String value;

  BillingType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static BillingType fromValue(String text) {
    for (BillingType b : BillingType.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}


package org.egov.inv.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.egov.inv.model.MaterialReceipt;
import org.egov.inv.model.RequestInfo;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Hold the Opening Balance request information.
 */
@ApiModel(description = "Hold the Opening Balance request information.")
@javax.annotation.Generated(value = "org.egov.inv.codegen.languages.SpringCodegen", date = "2017-11-08T13:51:07.770Z")

public class OpeningBalanceRequest   {
  @JsonProperty("RequestInfo")
  private RequestInfo requestInfo = null;

  @JsonProperty("MaterialReceipt")
  private List<MaterialReceipt> materialReceipt = new ArrayList<MaterialReceipt>();

  public OpeningBalanceRequest requestInfo(RequestInfo requestInfo) {
    this.requestInfo = requestInfo;
    return this;
  }

   /**
   * Get requestInfo
   * @return requestInfo
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public RequestInfo getRequestInfo() {
    return requestInfo;
  }

  public void setRequestInfo(RequestInfo requestInfo) {
    this.requestInfo = requestInfo;
  }

  public OpeningBalanceRequest materialReceipt(List<MaterialReceipt> materialReceipt) {
    this.materialReceipt = materialReceipt;
    return this;
  }

  public OpeningBalanceRequest addMaterialReceiptItem(MaterialReceipt materialReceiptItem) {
    this.materialReceipt.add(materialReceiptItem);
    return this;
  }

   /**
   * Get materialReceipt
   * @return materialReceipt
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public List<MaterialReceipt> getMaterialReceipt() {
    return materialReceipt;
  }

  public void setMaterialReceipt(List<MaterialReceipt> materialReceipt) {
    this.materialReceipt = materialReceipt;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpeningBalanceRequest openingBalanceRequest = (OpeningBalanceRequest) o;
    return Objects.equals(this.requestInfo, openingBalanceRequest.requestInfo) &&
        Objects.equals(this.materialReceipt, openingBalanceRequest.materialReceipt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestInfo, materialReceipt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpeningBalanceRequest {\n");
    
    sb.append("    requestInfo: ").append(toIndentedString(requestInfo)).append("\n");
    sb.append("    materialReceipt: ").append(toIndentedString(materialReceipt)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}


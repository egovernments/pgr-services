package org.egov.wcms.web.models;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.egov.wcms.web.models.Address;
import org.egov.wcms.web.models.AuditDetails;
import org.egov.wcms.web.models.BillingType;
import org.egov.wcms.web.models.Document;
import org.egov.wcms.web.models.Location;
import org.egov.wcms.web.models.Meter;
import org.egov.wcms.web.models.Property;
import org.egov.wcms.web.models.User;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

/**
 * water connection attributes
 */
@ApiModel(description = "water connection attributes")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2018-05-03T01:09:48.367+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Connection {
	@JsonProperty("uuid")
	private String uuid = null;

	/**
	 * possible and supported type of new water connection, default value will be
	 * \"PERMANENT\".
	 */
	public enum TypeEnum {
		TEMPORARY("TEMPORARY"),

		PERMANENT("PERMANENT");

		private String value;

		TypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static TypeEnum fromValue(String text) {
			for (TypeEnum b : TypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("type")
	private TypeEnum type = null;

	/**
	 * water connection status.
	 */
	public enum StatusEnum {
		ACTIVE("ACTIVE"),

		INACTIVE("INACTIVE"),

		INPROGRESS("INPROGRESS");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static StatusEnum fromValue(String text) {
			for (StatusEnum b : StatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("status")
	private StatusEnum status = null;

	@JsonProperty("acknowledgmentNumber")
	private String acknowledgmentNumber = null;

	@JsonProperty("connectionNumber")
	private String connectionNumber = null;

	@JsonProperty("oldConnectionNumber")
	private String oldConnectionNumber = null;

	@JsonProperty("applicationType")
	private String applicationType = null;

	@JsonProperty("billingType")
	private BillingType billingType = null;

	@JsonProperty("pipesize")
	private String pipesize = null;

	/**
	 * Water source type.
	 */
	public enum SourceTypeEnum {
		GROUNDWATER("GroundWater"),

		SURAFCEWATER("SurafceWater");

		private String value;

		SourceTypeEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static SourceTypeEnum fromValue(String text) {
			for (SourceTypeEnum b : SourceTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("sourceType")
	private SourceTypeEnum sourceType = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("numberOfTaps")
	private Long numberOfTaps = null;

	@JsonProperty("numberOfPersons")
	private Integer numberOfPersons = null;

	@JsonProperty("parentConnection")
	private String parentConnection = null;

	@JsonProperty("documents")
	@Valid
	private List<Document> documents = null;

	@JsonProperty("property")
	private Property property = null;

	@JsonProperty("address")
	private Address address = null;

	@JsonProperty("location")
	private Location location = null;

	@JsonProperty("meter")
	private Meter meter = null;

	@JsonProperty("owner")
	@Valid
	private List<User> owner = null;

	@JsonProperty("aditionalDetails")
	private Object aditionalDetails = null;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	public Connection addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}
		this.documents.add(documentsItem);
		return this;
	}

	public Connection addOwnerItem(User ownerItem) {
		if (this.owner == null) {
			this.owner = new ArrayList<>();
		}
		this.owner.add(ownerItem);
		return this;
	}

}

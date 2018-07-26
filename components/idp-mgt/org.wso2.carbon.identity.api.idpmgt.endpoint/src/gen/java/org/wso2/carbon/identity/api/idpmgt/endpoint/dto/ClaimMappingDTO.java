package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ClaimMappingDTO  {
  
  
  
  private String defaultValue = null;
  
  
  private ClaimDTO localClaim = null;
  
  
  private Boolean mandatory = null;
  
  
  private ClaimDTO remoteClaim = null;
  
  
  private Boolean required = null;

  
  /**
   * defaultValue
   **/
  @ApiModelProperty(value = "defaultValue")
  @JsonProperty("defaultValue")
  public String getDefaultValue() {
    return defaultValue;
  }
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("localClaim")
  public ClaimDTO getLocalClaim() {
    return localClaim;
  }
  public void setLocalClaim(ClaimDTO localClaim) {
    this.localClaim = localClaim;
  }

  
  /**
   * mandatory
   **/
  @ApiModelProperty(value = "mandatory")
  @JsonProperty("mandatory")
  public Boolean getMandatory() {
    return mandatory;
  }
  public void setMandatory(Boolean mandatory) {
    this.mandatory = mandatory;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("remoteClaim")
  public ClaimDTO getRemoteClaim() {
    return remoteClaim;
  }
  public void setRemoteClaim(ClaimDTO remoteClaim) {
    this.remoteClaim = remoteClaim;
  }

  
  /**
   * required
   **/
  @ApiModelProperty(value = "required")
  @JsonProperty("required")
  public Boolean getRequired() {
    return required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimMappingDTO {\n");
    
    sb.append("  defaultValue: ").append(defaultValue).append("\n");
    sb.append("  localClaim: ").append(localClaim).append("\n");
    sb.append("  mandatory: ").append(mandatory).append("\n");
    sb.append("  remoteClaim: ").append(remoteClaim).append("\n");
    sb.append("  required: ").append(required).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

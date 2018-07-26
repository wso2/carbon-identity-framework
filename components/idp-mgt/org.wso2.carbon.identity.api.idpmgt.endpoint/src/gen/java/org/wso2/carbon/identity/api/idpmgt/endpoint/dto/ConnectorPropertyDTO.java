package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConnectorPropertyDTO  {
  
  
  
  private Boolean advanced = null;
  
  
  private Boolean confidential = null;
  
  
  private String defaultValue = null;
  
  
  private String description = null;
  
  
  private String displayName = null;
  
  
  private Integer displayOrder = null;
  
  
  private String name = null;
  
  
  private Boolean required = null;
  
  
  private String type = null;
  
  
  private String value = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("advanced")
  public Boolean getAdvanced() {
    return advanced;
  }
  public void setAdvanced(Boolean advanced) {
    this.advanced = advanced;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("confidential")
  public Boolean getConfidential() {
    return confidential;
  }
  public void setConfidential(Boolean confidential) {
    this.confidential = confidential;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
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
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("displayOrder")
  public Integer getDisplayOrder() {
    return displayOrder;
  }
  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("required")
  public Boolean getRequired() {
    return required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConnectorPropertyDTO {\n");
    
    sb.append("  advanced: ").append(advanced).append("\n");
    sb.append("  confidential: ").append(confidential).append("\n");
    sb.append("  defaultValue: ").append(defaultValue).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  displayName: ").append(displayName).append("\n");
    sb.append("  displayOrder: ").append(displayOrder).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  required: ").append(required).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  value: ").append(value).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

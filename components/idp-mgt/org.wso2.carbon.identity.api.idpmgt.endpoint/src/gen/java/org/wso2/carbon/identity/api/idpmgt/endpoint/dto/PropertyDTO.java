package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class PropertyDTO  {
  
  
  
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
   * advanced
   **/
  @ApiModelProperty(value = "advanced")
  @JsonProperty("advanced")
  public Boolean getAdvanced() {
    return advanced;
  }
  public void setAdvanced(Boolean advanced) {
    this.advanced = advanced;
  }

  
  /**
   * confidential
   **/
  @ApiModelProperty(value = "confidential")
  @JsonProperty("confidential")
  public Boolean getConfidential() {
    return confidential;
  }
  public void setConfidential(Boolean confidential) {
    this.confidential = confidential;
  }

  
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
   * description
   **/
  @ApiModelProperty(value = "description")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * displayName
   **/
  @ApiModelProperty(value = "displayName")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  /**
   * displayOrder
   **/
  @ApiModelProperty(value = "displayOrder")
  @JsonProperty("displayOrder")
  public Integer getDisplayOrder() {
    return displayOrder;
  }
  public void setDisplayOrder(Integer displayOrder) {
    this.displayOrder = displayOrder;
  }

  
  /**
   * name
   **/
  @ApiModelProperty(value = "name")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
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

  
  /**
   * type
   **/
  @ApiModelProperty(value = "type")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   * value
   **/
  @ApiModelProperty(value = "value")
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
    sb.append("class PropertyDTO {\n");
    
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

package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class FederatedAuthenticatorConfigDTO  {
  
  
  
  private String displayName = null;
  
  
  private Boolean enabled = null;
  
  
  private String name = null;
  
  
  private List<PropertyDTO> propertyList = new ArrayList<PropertyDTO>();

  
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
   * enabled
   **/
  @ApiModelProperty(value = "enabled")
  @JsonProperty("enabled")
  public Boolean getEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
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
   * properties
   **/
  @ApiModelProperty(value = "properties")
  @JsonProperty("propertyList")
  public List<PropertyDTO> getPropertyList() {
    return propertyList;
  }
  public void setPropertyList(List<PropertyDTO> propertyList) {
    this.propertyList = propertyList;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class FederatedAuthenticatorConfigDTO {\n");
    
    sb.append("  displayName: ").append(displayName).append("\n");
    sb.append("  enabled: ").append(enabled).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  propertyList: ").append(propertyList).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

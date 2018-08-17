package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ProvisioningConnectorConfigDTO  {
  
  
  
  private Boolean blocking = null;
  
  
  private Boolean enabled = null;
  
  
  private String name = null;
  
  
  private List<PropertyDTO> provisioningProperties = new ArrayList<PropertyDTO>();
  
  
  private Boolean rulesEnabled = null;
  
  
  private Boolean valid = null;

  
  /**
   * claimId
   **/
  @ApiModelProperty(value = "claimId")
  @JsonProperty("blocking")
  public Boolean getBlocking() {
    return blocking;
  }
  public void setBlocking(Boolean blocking) {
    this.blocking = blocking;
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
   * object
   **/
  @ApiModelProperty(value = "object")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * provisioningProperties
   **/
  @ApiModelProperty(value = "provisioningProperties")
  @JsonProperty("provisioningProperties")
  public List<PropertyDTO> getProvisioningProperties() {
    return provisioningProperties;
  }
  public void setProvisioningProperties(List<PropertyDTO> provisioningProperties) {
    this.provisioningProperties = provisioningProperties;
  }

  
  /**
   * rulesEnabled
   **/
  @ApiModelProperty(value = "rulesEnabled")
  @JsonProperty("rulesEnabled")
  public Boolean getRulesEnabled() {
    return rulesEnabled;
  }
  public void setRulesEnabled(Boolean rulesEnabled) {
    this.rulesEnabled = rulesEnabled;
  }

  
  /**
   * valid
   **/
  @ApiModelProperty(value = "valid")
  @JsonProperty("valid")
  public Boolean getValid() {
    return valid;
  }
  public void setValid(Boolean valid) {
    this.valid = valid;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProvisioningConnectorConfigDTO {\n");
    
    sb.append("  blocking: ").append(blocking).append("\n");
    sb.append("  enabled: ").append(enabled).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  provisioningProperties: ").append(provisioningProperties).append("\n");
    sb.append("  rulesEnabled: ").append(rulesEnabled).append("\n");
    sb.append("  valid: ").append(valid).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

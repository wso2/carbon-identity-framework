package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningConnectorDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Provisioning configurations
 **/


@ApiModel(description = "Provisioning configurations")
public class ProvisioningDTO  {
  
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();
  
  
  private List<ProvisioningConnectorDTO> provisioningConnectors = new ArrayList<ProvisioningConnectorDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("properties")
  public List<PropertyDTO> getProperties() {
    return properties;
  }
  public void setProperties(List<PropertyDTO> properties) {
    this.properties = properties;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("provisioningConnectors")
  public List<ProvisioningConnectorDTO> getProvisioningConnectors() {
    return provisioningConnectors;
  }
  public void setProvisioningConnectors(List<ProvisioningConnectorDTO> provisioningConnectors) {
    this.provisioningConnectors = provisioningConnectors;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProvisioningDTO {\n");
    
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("  provisioningConnectors: ").append(provisioningConnectors).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimMappingDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Claim configurations
 **/


@ApiModel(description = "Claim configurations")
public class ClaimsDTO  {
  
  
  
  private List<ClaimMappingDTO> claimMapping = new ArrayList<ClaimMappingDTO>();
  
  
  private Boolean localDialect = null;
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("claimMapping")
  public List<ClaimMappingDTO> getClaimMapping() {
    return claimMapping;
  }
  public void setClaimMapping(List<ClaimMappingDTO> claimMapping) {
    this.claimMapping = claimMapping;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("localDialect")
  public Boolean getLocalDialect() {
    return localDialect;
  }
  public void setLocalDialect(Boolean localDialect) {
    this.localDialect = localDialect;
  }

  
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimsDTO {\n");
    
    sb.append("  claimMapping: ").append(claimMapping).append("\n");
    sb.append("  localDialect: ").append(localDialect).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

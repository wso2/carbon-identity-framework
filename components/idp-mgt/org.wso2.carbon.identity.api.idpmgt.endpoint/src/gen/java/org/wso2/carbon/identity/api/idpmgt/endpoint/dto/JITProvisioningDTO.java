package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * JIT provisioning related configurations
 **/


@ApiModel(description = "JIT provisioning related configurations")
public class JITProvisioningDTO  {
  
  
  
  private Boolean enable = null;
  
  
  private String userstoreDomain = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enable")
  public Boolean getEnable() {
    return enable;
  }
  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("userstoreDomain")
  public String getUserstoreDomain() {
    return userstoreDomain;
  }
  public void setUserstoreDomain(String userstoreDomain) {
    this.userstoreDomain = userstoreDomain;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JITProvisioningDTO {\n");
    
    sb.append("  enable: ").append(enable).append("\n");
    sb.append("  userstoreDomain: ").append(userstoreDomain).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.AuthenticatorDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.JITProvisioningDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Authentication related configurations
 **/


@ApiModel(description = "Authentication related configurations")
public class AuthenticationDTO  {
  
  
  
  private JITProvisioningDTO jitProvisioning = null;
  
  
  private List<AuthenticatorDTO> authenticators = new ArrayList<AuthenticatorDTO>();
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("jitProvisioning")
  public JITProvisioningDTO getJitProvisioning() {
    return jitProvisioning;
  }
  public void setJitProvisioning(JITProvisioningDTO jitProvisioning) {
    this.jitProvisioning = jitProvisioning;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("authenticators")
  public List<AuthenticatorDTO> getAuthenticators() {
    return authenticators;
  }
  public void setAuthenticators(List<AuthenticatorDTO> authenticators) {
    this.authenticators = authenticators;
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
    sb.append("class AuthenticationDTO {\n");
    
    sb.append("  jitProvisioning: ").append(jitProvisioning).append("\n");
    sb.append("  authenticators: ").append(authenticators).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

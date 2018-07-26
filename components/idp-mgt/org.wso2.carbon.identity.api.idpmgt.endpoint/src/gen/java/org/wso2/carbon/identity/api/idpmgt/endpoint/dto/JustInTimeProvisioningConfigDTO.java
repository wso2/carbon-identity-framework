package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class JustInTimeProvisioningConfigDTO  {
  
  
  
  private Boolean modifyUserNameAllowed = null;
  
  
  private Boolean passwordProvisioningEnabled = null;
  
  
  private Boolean promptConsent = null;
  
  
  private String userStoreClaimUri = null;

  
  /**
   * modifyUserNameAllowed
   **/
  @ApiModelProperty(value = "modifyUserNameAllowed")
  @JsonProperty("modifyUserNameAllowed")
  public Boolean getModifyUserNameAllowed() {
    return modifyUserNameAllowed;
  }
  public void setModifyUserNameAllowed(Boolean modifyUserNameAllowed) {
    this.modifyUserNameAllowed = modifyUserNameAllowed;
  }

  
  /**
   * passwordProvisioningEnabled
   **/
  @ApiModelProperty(value = "passwordProvisioningEnabled")
  @JsonProperty("passwordProvisioningEnabled")
  public Boolean getPasswordProvisioningEnabled() {
    return passwordProvisioningEnabled;
  }
  public void setPasswordProvisioningEnabled(Boolean passwordProvisioningEnabled) {
    this.passwordProvisioningEnabled = passwordProvisioningEnabled;
  }

  
  /**
   * promptConsent
   **/
  @ApiModelProperty(value = "promptConsent")
  @JsonProperty("promptConsent")
  public Boolean getPromptConsent() {
    return promptConsent;
  }
  public void setPromptConsent(Boolean promptConsent) {
    this.promptConsent = promptConsent;
  }

  
  /**
   * userStoreClaimUri
   **/
  @ApiModelProperty(value = "userStoreClaimUri")
  @JsonProperty("userStoreClaimUri")
  public String getUserStoreClaimUri() {
    return userStoreClaimUri;
  }
  public void setUserStoreClaimUri(String userStoreClaimUri) {
    this.userStoreClaimUri = userStoreClaimUri;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class JustInTimeProvisioningConfigDTO {\n");
    
    sb.append("  modifyUserNameAllowed: ").append(modifyUserNameAllowed).append("\n");
    sb.append("  passwordProvisioningEnabled: ").append(passwordProvisioningEnabled).append("\n");
    sb.append("  promptConsent: ").append(promptConsent).append("\n");
    sb.append("  userStoreClaimUri: ").append(userStoreClaimUri).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

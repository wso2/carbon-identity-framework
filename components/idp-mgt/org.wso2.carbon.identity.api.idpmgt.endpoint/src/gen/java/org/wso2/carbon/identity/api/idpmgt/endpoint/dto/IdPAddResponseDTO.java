package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class IdPAddResponseDTO  {
  
  
  @NotNull
  private String idPID = null;
  
  
  private String language = null;
  
  @NotNull
  private String idPName = null;
  
  @NotNull
  private String tenantDomain = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("idPID")
  public String getIdPID() {
    return idPID;
  }
  public void setIdPID(String idPID) {
    this.idPID = idPID;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("language")
  public String getLanguage() {
    return language;
  }
  public void setLanguage(String language) {
    this.language = language;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("idPName")
  public String getIdPName() {
    return idPName;
  }
  public void setIdPName(String idPName) {
    this.idPName = idPName;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdPAddResponseDTO {\n");
    
    sb.append("  idPID: ").append(idPID).append("\n");
    sb.append("  language: ").append(language).append("\n");
    sb.append("  idPName: ").append(idPName).append("\n");
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

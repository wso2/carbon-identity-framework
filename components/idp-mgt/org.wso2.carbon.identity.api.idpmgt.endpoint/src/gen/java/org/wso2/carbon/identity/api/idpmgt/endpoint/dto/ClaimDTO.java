package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ClaimDTO  {
  
  
  @NotNull
  private Integer claimId = null;
  
  @NotNull
  private String claimUri = null;

  
  /**
   * claimId
   **/
  @ApiModelProperty(required = true, value = "claimId")
  @JsonProperty("claimId")
  public Integer getClaimId() {
    return claimId;
  }
  public void setClaimId(Integer claimId) {
    this.claimId = claimId;
  }

  
  /**
   * claimUri
   **/
  @ApiModelProperty(required = true, value = "claimUri")
  @JsonProperty("claimUri")
  public String getClaimUri() {
    return claimUri;
  }
  public void setClaimUri(String claimUri) {
    this.claimUri = claimUri;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimDTO {\n");
    
    sb.append("  claimId: ").append(claimId).append("\n");
    sb.append("  claimUri: ").append(claimUri).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

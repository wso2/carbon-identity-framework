package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimMappingDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ClaimConfigDTO  {
  
  
  
  private Boolean alwaysSendMappedLocalSubjectId = null;
  
  
  private List<ClaimMappingDTO> claimMappings = new ArrayList<ClaimMappingDTO>();
  
  
  private List<ClaimDTO> idpClaims = new ArrayList<ClaimDTO>();
  
  
  private Boolean localClaimDialect = null;
  
  
  private String roleClaimURI = null;
  
  
  private List<String> spClaimDialects = new ArrayList<String>();
  
  
  private String userClaimURI = null;

  
  /**
   * alwaysSendMappedLocalSubjectId
   **/
  @ApiModelProperty(value = "alwaysSendMappedLocalSubjectId")
  @JsonProperty("alwaysSendMappedLocalSubjectId")
  public Boolean getAlwaysSendMappedLocalSubjectId() {
    return alwaysSendMappedLocalSubjectId;
  }
  public void setAlwaysSendMappedLocalSubjectId(Boolean alwaysSendMappedLocalSubjectId) {
    this.alwaysSendMappedLocalSubjectId = alwaysSendMappedLocalSubjectId;
  }

  
  /**
   * claimMappings
   **/
  @ApiModelProperty(value = "claimMappings")
  @JsonProperty("claimMappings")
  public List<ClaimMappingDTO> getClaimMappings() {
    return claimMappings;
  }
  public void setClaimMappings(List<ClaimMappingDTO> claimMappings) {
    this.claimMappings = claimMappings;
  }

  
  /**
   * claimMappings
   **/
  @ApiModelProperty(value = "claimMappings")
  @JsonProperty("idpClaims")
  public List<ClaimDTO> getIdpClaims() {
    return idpClaims;
  }
  public void setIdpClaims(List<ClaimDTO> idpClaims) {
    this.idpClaims = idpClaims;
  }

  
  /**
   * localClaimDialect
   **/
  @ApiModelProperty(value = "localClaimDialect")
  @JsonProperty("localClaimDialect")
  public Boolean getLocalClaimDialect() {
    return localClaimDialect;
  }
  public void setLocalClaimDialect(Boolean localClaimDialect) {
    this.localClaimDialect = localClaimDialect;
  }

  
  /**
   * roleClaimURI
   **/
  @ApiModelProperty(value = "roleClaimURI")
  @JsonProperty("roleClaimURI")
  public String getRoleClaimURI() {
    return roleClaimURI;
  }
  public void setRoleClaimURI(String roleClaimURI) {
    this.roleClaimURI = roleClaimURI;
  }

  
  /**
   * spClaimDialects
   **/
  @ApiModelProperty(value = "spClaimDialects")
  @JsonProperty("spClaimDialects")
  public List<String> getSpClaimDialects() {
    return spClaimDialects;
  }
  public void setSpClaimDialects(List<String> spClaimDialects) {
    this.spClaimDialects = spClaimDialects;
  }

  
  /**
   * userClaimURI
   **/
  @ApiModelProperty(value = "userClaimURI")
  @JsonProperty("userClaimURI")
  public String getUserClaimURI() {
    return userClaimURI;
  }
  public void setUserClaimURI(String userClaimURI) {
    this.userClaimURI = userClaimURI;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClaimConfigDTO {\n");
    
    sb.append("  alwaysSendMappedLocalSubjectId: ").append(alwaysSendMappedLocalSubjectId).append("\n");
    sb.append("  claimMappings: ").append(claimMappings).append("\n");
    sb.append("  idpClaims: ").append(idpClaims).append("\n");
    sb.append("  localClaimDialect: ").append(localClaimDialect).append("\n");
    sb.append("  roleClaimURI: ").append(roleClaimURI).append("\n");
    sb.append("  spClaimDialects: ").append(spClaimDialects).append("\n");
    sb.append("  userClaimURI: ").append(userClaimURI).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.AuthenticationDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimsDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.KeysDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.RolesDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class IdentityProviderDTO  {
  
  
  @NotNull
  private String id = null;
  
  
  private String name = null;
  
  
  private String displayName = null;
  
  
  private String description = null;
  
  
  private String homeRealmIdentifier = null;
  
  
  private Boolean federationHub = null;
  
  
  private AuthenticationDTO authentication = null;
  
  
  private ProvisioningDTO provisioning = null;
  
  
  private ClaimsDTO claims = null;
  
  
  private RolesDTO roles = null;
  
  
  private KeysDTO keys = null;
  
  
  private List<PropertyDTO> properties = new ArrayList<PropertyDTO>();

  
  /**
   * A unique ID for an Identity Provider
   **/
  @ApiModelProperty(required = true, value = "A unique ID for an Identity Provider")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("homeRealmIdentifier")
  public String getHomeRealmIdentifier() {
    return homeRealmIdentifier;
  }
  public void setHomeRealmIdentifier(String homeRealmIdentifier) {
    this.homeRealmIdentifier = homeRealmIdentifier;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("federationHub")
  public Boolean getFederationHub() {
    return federationHub;
  }
  public void setFederationHub(Boolean federationHub) {
    this.federationHub = federationHub;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("authentication")
  public AuthenticationDTO getAuthentication() {
    return authentication;
  }
  public void setAuthentication(AuthenticationDTO authentication) {
    this.authentication = authentication;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("provisioning")
  public ProvisioningDTO getProvisioning() {
    return provisioning;
  }
  public void setProvisioning(ProvisioningDTO provisioning) {
    this.provisioning = provisioning;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("claims")
  public ClaimsDTO getClaims() {
    return claims;
  }
  public void setClaims(ClaimsDTO claims) {
    this.claims = claims;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("roles")
  public RolesDTO getRoles() {
    return roles;
  }
  public void setRoles(RolesDTO roles) {
    this.roles = roles;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("keys")
  public KeysDTO getKeys() {
    return keys;
  }
  public void setKeys(KeysDTO keys) {
    this.keys = keys;
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
    sb.append("class IdentityProviderDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  displayName: ").append(displayName).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  homeRealmIdentifier: ").append(homeRealmIdentifier).append("\n");
    sb.append("  federationHub: ").append(federationHub).append("\n");
    sb.append("  authentication: ").append(authentication).append("\n");
    sb.append("  provisioning: ").append(provisioning).append("\n");
    sb.append("  claims: ").append(claims).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("  keys: ").append(keys).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

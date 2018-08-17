package org.wso2.carbon.identity.api.idpmgt.endpoint.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.FederatedAuthenticatorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdentityProviderPropertyDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.JustInTimeProvisioningConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PermissionsAndRoleConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningConnectorConfigDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class IdPDetailDTO  {
  
  
  
  private String alias = null;
  
  
  private String certificate = null;
  
  
  private ClaimConfigDTO claimConfig = null;
  
  
  private FederatedAuthenticatorConfigDTO defaultAuthenticatorConfig = null;
  
  
  private ProvisioningConnectorConfigDTO defaultProvisioningConnectorConfig = null;
  
  @NotNull
  private String displayName = null;
  
  
  private Boolean enable = null;
  
  
  private List<FederatedAuthenticatorConfigDTO> federatedAuthenticatorConfigs = new ArrayList<FederatedAuthenticatorConfigDTO>();
  
  @NotNull
  private Boolean federationHub = null;
  
  
  private String homeRealmId = null;
  
  
  private String id = null;
  
  
  private String identityProviderDescription = null;
  
  
  private String identityProviderName = null;
  
  
  private List<IdentityProviderPropertyDTO> idpProperties = new ArrayList<IdentityProviderPropertyDTO>();
  
  
  private JustInTimeProvisioningConfigDTO justInTimeProvisioningConfig = null;
  
  
  private PermissionsAndRoleConfigDTO permissionAndRoleConfig = null;
  
  
  private Boolean primary = null;
  
  
  private List<ProvisioningConnectorConfigDTO> provisioningConnectorConfigs = new ArrayList<ProvisioningConnectorConfigDTO>();
  
  
  private String provisioningRole = null;

  
  /**
   * alias
   **/
  @ApiModelProperty(value = "alias")
  @JsonProperty("alias")
  public String getAlias() {
    return alias;
  }
  public void setAlias(String alias) {
    this.alias = alias;
  }

  
  /**
   * certificate
   **/
  @ApiModelProperty(value = "certificate")
  @JsonProperty("certificate")
  public String getCertificate() {
    return certificate;
  }
  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("claimConfig")
  public ClaimConfigDTO getClaimConfig() {
    return claimConfig;
  }
  public void setClaimConfig(ClaimConfigDTO claimConfig) {
    this.claimConfig = claimConfig;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("defaultAuthenticatorConfig")
  public FederatedAuthenticatorConfigDTO getDefaultAuthenticatorConfig() {
    return defaultAuthenticatorConfig;
  }
  public void setDefaultAuthenticatorConfig(FederatedAuthenticatorConfigDTO defaultAuthenticatorConfig) {
    this.defaultAuthenticatorConfig = defaultAuthenticatorConfig;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("defaultProvisioningConnectorConfig")
  public ProvisioningConnectorConfigDTO getDefaultProvisioningConnectorConfig() {
    return defaultProvisioningConnectorConfig;
  }
  public void setDefaultProvisioningConnectorConfig(ProvisioningConnectorConfigDTO defaultProvisioningConnectorConfig) {
    this.defaultProvisioningConnectorConfig = defaultProvisioningConnectorConfig;
  }

  
  /**
   * displayName
   **/
  @ApiModelProperty(required = true, value = "displayName")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  /**
   * enable
   **/
  @ApiModelProperty(value = "enable")
  @JsonProperty("enable")
  public Boolean getEnable() {
    return enable;
  }
  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  
  /**
   * federatedAuthenticatorConfigs
   **/
  @ApiModelProperty(value = "federatedAuthenticatorConfigs")
  @JsonProperty("federatedAuthenticatorConfigs")
  public List<FederatedAuthenticatorConfigDTO> getFederatedAuthenticatorConfigs() {
    return federatedAuthenticatorConfigs;
  }
  public void setFederatedAuthenticatorConfigs(List<FederatedAuthenticatorConfigDTO> federatedAuthenticatorConfigs) {
    this.federatedAuthenticatorConfigs = federatedAuthenticatorConfigs;
  }

  
  /**
   * federationHub
   **/
  @ApiModelProperty(required = true, value = "federationHub")
  @JsonProperty("federationHub")
  public Boolean getFederationHub() {
    return federationHub;
  }
  public void setFederationHub(Boolean federationHub) {
    this.federationHub = federationHub;
  }

  
  /**
   * homeRealmId
   **/
  @ApiModelProperty(value = "homeRealmId")
  @JsonProperty("homeRealmId")
  public String getHomeRealmId() {
    return homeRealmId;
  }
  public void setHomeRealmId(String homeRealmId) {
    this.homeRealmId = homeRealmId;
  }

  
  /**
   * id
   **/
  @ApiModelProperty(value = "id")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * identityProviderDescription
   **/
  @ApiModelProperty(value = "identityProviderDescription")
  @JsonProperty("identityProviderDescription")
  public String getIdentityProviderDescription() {
    return identityProviderDescription;
  }
  public void setIdentityProviderDescription(String identityProviderDescription) {
    this.identityProviderDescription = identityProviderDescription;
  }

  
  /**
   * identityProviderName
   **/
  @ApiModelProperty(value = "identityProviderName")
  @JsonProperty("identityProviderName")
  public String getIdentityProviderName() {
    return identityProviderName;
  }
  public void setIdentityProviderName(String identityProviderName) {
    this.identityProviderName = identityProviderName;
  }

  
  /**
   * idpProperties
   **/
  @ApiModelProperty(value = "idpProperties")
  @JsonProperty("idpProperties")
  public List<IdentityProviderPropertyDTO> getIdpProperties() {
    return idpProperties;
  }
  public void setIdpProperties(List<IdentityProviderPropertyDTO> idpProperties) {
    this.idpProperties = idpProperties;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("justInTimeProvisioningConfig")
  public JustInTimeProvisioningConfigDTO getJustInTimeProvisioningConfig() {
    return justInTimeProvisioningConfig;
  }
  public void setJustInTimeProvisioningConfig(JustInTimeProvisioningConfigDTO justInTimeProvisioningConfig) {
    this.justInTimeProvisioningConfig = justInTimeProvisioningConfig;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("permissionAndRoleConfig")
  public PermissionsAndRoleConfigDTO getPermissionAndRoleConfig() {
    return permissionAndRoleConfig;
  }
  public void setPermissionAndRoleConfig(PermissionsAndRoleConfigDTO permissionAndRoleConfig) {
    this.permissionAndRoleConfig = permissionAndRoleConfig;
  }

  
  /**
   * primary
   **/
  @ApiModelProperty(value = "primary")
  @JsonProperty("primary")
  public Boolean getPrimary() {
    return primary;
  }
  public void setPrimary(Boolean primary) {
    this.primary = primary;
  }

  
  /**
   * provisioningConnectorConfigs
   **/
  @ApiModelProperty(value = "provisioningConnectorConfigs")
  @JsonProperty("provisioningConnectorConfigs")
  public List<ProvisioningConnectorConfigDTO> getProvisioningConnectorConfigs() {
    return provisioningConnectorConfigs;
  }
  public void setProvisioningConnectorConfigs(List<ProvisioningConnectorConfigDTO> provisioningConnectorConfigs) {
    this.provisioningConnectorConfigs = provisioningConnectorConfigs;
  }

  
  /**
   * provisioningRole
   **/
  @ApiModelProperty(value = "provisioningRole")
  @JsonProperty("provisioningRole")
  public String getProvisioningRole() {
    return provisioningRole;
  }
  public void setProvisioningRole(String provisioningRole) {
    this.provisioningRole = provisioningRole;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class IdPDetailDTO {\n");
    
    sb.append("  alias: ").append(alias).append("\n");
    sb.append("  certificate: ").append(certificate).append("\n");
    sb.append("  claimConfig: ").append(claimConfig).append("\n");
    sb.append("  defaultAuthenticatorConfig: ").append(defaultAuthenticatorConfig).append("\n");
    sb.append("  defaultProvisioningConnectorConfig: ").append(defaultProvisioningConnectorConfig).append("\n");
    sb.append("  displayName: ").append(displayName).append("\n");
    sb.append("  enable: ").append(enable).append("\n");
    sb.append("  federatedAuthenticatorConfigs: ").append(federatedAuthenticatorConfigs).append("\n");
    sb.append("  federationHub: ").append(federationHub).append("\n");
    sb.append("  homeRealmId: ").append(homeRealmId).append("\n");
    sb.append("  id: ").append(id).append("\n");
    sb.append("  identityProviderDescription: ").append(identityProviderDescription).append("\n");
    sb.append("  identityProviderName: ").append(identityProviderName).append("\n");
    sb.append("  idpProperties: ").append(idpProperties).append("\n");
    sb.append("  justInTimeProvisioningConfig: ").append(justInTimeProvisioningConfig).append("\n");
    sb.append("  permissionAndRoleConfig: ").append(permissionAndRoleConfig).append("\n");
    sb.append("  primary: ").append(primary).append("\n");
    sb.append("  provisioningConnectorConfigs: ").append(provisioningConnectorConfigs).append("\n");
    sb.append("  provisioningRole: ").append(provisioningRole).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.api.idpmgt.endpoint;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.wso2.carbon.identity.api.idpmgt.IDPMgtBridgeServiceClientException;
import org.wso2.carbon.identity.api.idpmgt.IdPConstants;
import org.wso2.carbon.identity.api.idpmgt.endpoint.Exceptions.BadRequestException;
import org.wso2.carbon.identity.api.idpmgt.endpoint.Exceptions.InternalServerErrorException;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ApplicationPermissionDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ClaimMappingDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ErrorDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.FederatedAuthenticatorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdPDetailDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.IdentityProviderPropertyDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.JustInTimeProvisioningConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.LocalRoleDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PermissionsAndRoleConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.PropertyDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.ProvisioningConnectorConfigDTO;
import org.wso2.carbon.identity.api.idpmgt.endpoint.dto.RoleMappingDTO;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EndpointUtils {

    private EndpointUtils() {

    }

    /**
     * Translate identityProvider to identityProviderDTO
     *
     * @param identityProvider identity provider that needs to be translated to identity provider DTO
     * @return IdPDetailDTO
     */
    public static IdPDetailDTO translateIDPToIDPDetail(IdentityProvider identityProvider) {

        if (identityProvider == null) {
            return null;
        }
        IdPDetailDTO identityProviderDTO = new IdPDetailDTO();

        //Basic information related to Identity Provider
        identityProviderDTO.setDisplayName(identityProvider.getIdentityProviderName());
        identityProviderDTO.setEnable(identityProvider.isEnable());
        identityProviderDTO.setHomeRealmId(identityProvider.getHomeRealmId());
        identityProviderDTO.setId(identityProvider.getId());
        identityProviderDTO.setAlias(identityProvider.getAlias());
        identityProviderDTO.setFederationHub(identityProvider.isFederationHub());
        identityProviderDTO.setCertificate(identityProvider.getCertificate());
        identityProviderDTO.setPrimary(identityProvider.isPrimary());
        identityProviderDTO.setProvisioningRole(identityProvider.getProvisioningRole());
        identityProviderDTO.setIdentityProviderName(identityProvider.getIdentityProviderName());
        identityProviderDTO.setIdentityProviderDescription(identityProvider.getIdentityProviderDescription());

        //Default authenticated configurations
        FederatedAuthenticatorConfig federatedAuthenticatorConfig = identityProvider
                .getDefaultAuthenticatorConfig();
        identityProviderDTO.setDefaultAuthenticatorConfig(createDefaultAuthenticatorDTO(federatedAuthenticatorConfig));

        //Claim configurations
        ClaimConfig claimConfig = identityProvider.getClaimConfig();
        identityProviderDTO.setClaimConfig(createClaimConfigDTO(claimConfig));

        //Provisioning Connector Configuration
        ProvisioningConnectorConfig provisioningConnectorConfig = identityProvider
                .getDefaultProvisioningConnectorConfig();
        identityProviderDTO.setDefaultProvisioningConnectorConfig(createProvisioningConnectorConfigDTO
                (provisioningConnectorConfig));

        //Federated authenticator configurations
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = identityProvider
                .getFederatedAuthenticatorConfigs();
        if (ArrayUtils.isNotEmpty(federatedAuthenticatorConfigs)) {
            List<FederatedAuthenticatorConfigDTO> federatedAuthenticatorConfigDTOs = createFederatorAuthenticatorDTOList
                    (Arrays.asList(federatedAuthenticatorConfigs));
            identityProviderDTO.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigDTOs);
        }
        //Identity Provider Properties
        IdentityProviderProperty[] identityProviderProperties = identityProvider.getIdpProperties();
        if (ArrayUtils.isNotEmpty(identityProviderProperties)) {
            List<IdentityProviderPropertyDTO> identityProviderPropertyDTOs = createIdentityProviderDTOProperties
                    (Arrays.asList(identityProviderProperties));
            identityProviderDTO.setIdpProperties(identityProviderPropertyDTOs);
        }
        // JustInTime Provisioning Configurations
        JustInTimeProvisioningConfigDTO justInTimeProvisioningConfigDTO = createJustinTimeProvisioningConfigDTO
                (identityProvider.getJustInTimeProvisioningConfig());
        identityProviderDTO.setJustInTimeProvisioningConfig(justInTimeProvisioningConfigDTO);

        //Permissions And Role Configurations
        identityProviderDTO.setPermissionAndRoleConfig(createPermissionAndRoleConfigDTO(identityProvider
                .getPermissionAndRoleConfig()));

        //Provisioning Connector Configurations
        ProvisioningConnectorConfig[] provisioningConnectorConfigs = identityProvider
                .getProvisioningConnectorConfigs();
        if (ArrayUtils.isNotEmpty(provisioningConnectorConfigs)) {
            List<ProvisioningConnectorConfigDTO> provisioningConnectorConfigDTOs = createProvisioningConnectorConfigDTOs
                    (Arrays.asList(provisioningConnectorConfigs));
            identityProviderDTO.setProvisioningConnectorConfigs(provisioningConnectorConfigDTOs);
        }
        return identityProviderDTO;
    }

    /**
     * Translate identityProviderDTP to identityProvider
     *
     * @param identityProviderDTO identity providerDTO that needs to be translated to identity provider
     * @return IdPDetail
     */
    public static IdentityProvider translateIDPDetailToIDP(IdPDetailDTO identityProviderDTO) {

        if (identityProviderDTO == null) {
            return null;
        }
        IdentityProvider identityProvider = new IdentityProvider();

        //Basic information related to Identity Provider
        identityProvider.setIdentityProviderName(identityProviderDTO.getIdentityProviderName());
        identityProvider.setId(identityProviderDTO.getId());
        identityProvider.setAlias(identityProviderDTO.getAlias());
        identityProvider.setCertificate(identityProviderDTO.getCertificate());
        if (identityProviderDTO.getEnable() != null) {
            identityProvider.setEnable(identityProviderDTO.getEnable());
        }
        if (identityProviderDTO.getFederationHub() != null) {
            identityProvider.setFederationHub(identityProviderDTO.getFederationHub());
        }
        identityProvider.setIdentityProviderDescription(identityProviderDTO.getIdentityProviderDescription());
        if (identityProviderDTO.getPrimary() != null) {
            identityProvider.setPrimary(identityProviderDTO.getPrimary());
        }
        identityProvider.setProvisioningRole(identityProviderDTO.getProvisioningRole());
        identityProvider.setDisplayName(identityProviderDTO.getDisplayName());
        identityProvider.setHomeRealmId(identityProviderDTO.getHomeRealmId());

        //Default authenticated configurations
        FederatedAuthenticatorConfigDTO federatedAuthenticatorConfigDTO = identityProviderDTO
                .getDefaultAuthenticatorConfig();
        identityProvider.setDefaultAuthenticatorConfig(createDefaultAuthenticator(federatedAuthenticatorConfigDTO));

        //Claim configurations
        ClaimConfigDTO claimConfigDTO = identityProviderDTO.getClaimConfig();
        identityProvider.setClaimConfig(createClaimConfig(claimConfigDTO));

        //Provisioning Connector Configuration
        ProvisioningConnectorConfigDTO provisioningConnectorConfigDTO = identityProviderDTO
                .getDefaultProvisioningConnectorConfig();
        identityProvider.setDefaultProvisioningConnectorConfig(createProvisioningConnectorConfig
                (provisioningConnectorConfigDTO));

        //Federated authenticator configurations
        List<FederatedAuthenticatorConfigDTO> federatedAuthenticatorConfigDTOS = identityProviderDTO
                .getFederatedAuthenticatorConfigs();
        List<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs = createFederatorAuthenticatorList
                (federatedAuthenticatorConfigDTOS);
        identityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticatorConfigs.toArray(new
                FederatedAuthenticatorConfig[0]));

        //Identity Provider Properties
        List<IdentityProviderPropertyDTO> identityProviderPropertyDTOS = identityProviderDTO.getIdpProperties();
        List<IdentityProviderProperty> identityProviderProperties = createIdentityProviderProperties
                (identityProviderPropertyDTOS);
        identityProvider.setIdpProperties(identityProviderProperties.toArray(new IdentityProviderProperty[0]));

        // JustInTime Provisioning Configurations
        JustInTimeProvisioningConfig justInTimeProvisioningConfig = createJustinTimeProvisioningConfig
                (identityProviderDTO.getJustInTimeProvisioningConfig());
        identityProvider.setJustInTimeProvisioningConfig(justInTimeProvisioningConfig);

        //Permissions And Role Configurations
        identityProvider.setPermissionAndRoleConfig(createPermissionAndRoleConfig(identityProviderDTO
                .getPermissionAndRoleConfig()));

        //Provisioning Connector Configurations
        List<ProvisioningConnectorConfigDTO> provisioningConnectorConfigDTOs = identityProviderDTO
                .getProvisioningConnectorConfigs();
        List<ProvisioningConnectorConfig> provisioningConnectorConfigs = createProvisioningConnectorConfigs
                (provisioningConnectorConfigDTOs);
        identityProvider.setProvisioningConnectorConfigs(provisioningConnectorConfigs.toArray(new
                ProvisioningConnectorConfig[0]));

        return identityProvider;
    }

    /**
     * Translate identityProvider list to identityProviderDTO list
     *
     * @param idpList identity provider list that needs to be translated to identity provider DTO list
     * @return IdPDetailDTO list
     */
    public static List<IdPDetailDTO> translateIDPDetailList(List<IdentityProvider> idpList) {

        List<IdPDetailDTO> idpListResponseDTOList = new ArrayList<>();
        if (idpList != null) {
            idpList.forEach(idp -> idpListResponseDTOList.add(translateIDPToIDPDetail(idp)));
        }
        return idpListResponseDTOList;
    }

    /**
     * Translate provision connector list to provision connector DTO list list
     *
     * @param provisioningConnectorConfigDTOs provision connector list that needs to be translated to provision
     *                                        connector DTO list
     * @return provisioningConnectorConfigs list
     */
    private static List<ProvisioningConnectorConfig> createProvisioningConnectorConfigs
    (List<ProvisioningConnectorConfigDTO> provisioningConnectorConfigDTOs) {

        List<ProvisioningConnectorConfig> provisioningConnectorConfigs = new ArrayList<>();
        if (provisioningConnectorConfigDTOs != null) {
            provisioningConnectorConfigDTOs.forEach(provisioningConnectorConfigDTO ->
                    provisioningConnectorConfigs.add(createProvisioningConnectorConfig(provisioningConnectorConfigDTO)));
        }
        return provisioningConnectorConfigs;
    }

    /**
     * Translate provision connector DTO list to provision connector list
     *
     * @param provisioningConnectorConfigs provision connector DTO list that needs to be translated to provision
     *                                     connector list
     * @return provisioningConnectorConfigDTOs list
     */
    public static List<ProvisioningConnectorConfigDTO> createProvisioningConnectorConfigDTOs
    (List<ProvisioningConnectorConfig> provisioningConnectorConfigs) {

        List<ProvisioningConnectorConfigDTO> provisioningConnectorConfigDTOs = new ArrayList<>();
        if (provisioningConnectorConfigs != null) {
            provisioningConnectorConfigs.forEach(provisioningConnectorConfig ->
                    provisioningConnectorConfigDTOs.add(createProvisioningConnectorConfigDTO(provisioningConnectorConfig)));
        }
        return provisioningConnectorConfigDTOs;
    }

    /**
     * Translate role configuration DTO to role config
     *
     * @param permissionsAndRoleConfigDTO role config DTO that needs to be translated to role config
     * @return permissionsAndRoleConfig
     */
    public static PermissionsAndRoleConfig createPermissionAndRoleConfig(PermissionsAndRoleConfigDTO
                                                                                 permissionsAndRoleConfigDTO) {

        PermissionsAndRoleConfig permissionsAndRoleConfig = null;

        if (permissionsAndRoleConfigDTO != null) {
            permissionsAndRoleConfig = new PermissionsAndRoleConfig();
            permissionsAndRoleConfig.setIdpRoles(permissionsAndRoleConfigDTO.getIdpRoles().toArray(new String[0]));

            List<ApplicationPermissionDTO> applicationPermissionDTOS = permissionsAndRoleConfigDTO.getPermissions();
            List<ApplicationPermission> applicationPermissions = new ArrayList<>();
            for (ApplicationPermissionDTO applicationPermissionDTO : applicationPermissionDTOS) {
                ApplicationPermission applicationPermission = new ApplicationPermission();
                applicationPermission.setValue(applicationPermissionDTO.getValue());
                applicationPermissions.add(applicationPermission);
            }
            permissionsAndRoleConfig.setPermissions(applicationPermissions.toArray(new ApplicationPermission[0]));

            List<RoleMappingDTO> roleMappingDTOS = permissionsAndRoleConfigDTO.getRoleMappings();
            List<RoleMapping> roleMappings = new ArrayList<>();
            for (RoleMappingDTO roleMappingDTO : roleMappingDTOS) {
                RoleMapping roleMapping = new RoleMapping();
                roleMapping.setLocalRole(new LocalRole(roleMappingDTO.getLocalRole().getUserStoreId(), roleMappingDTO
                        .getLocalRole().getLocalRoleName()));
                roleMapping.setRemoteRole(roleMappingDTO.getRemoteRole());
                roleMappings.add(roleMapping);
            }
            permissionsAndRoleConfig.setRoleMappings(roleMappings.toArray(new RoleMapping[0]));
        }
        return permissionsAndRoleConfig;
    }

    /**
     * Translate role configuration to role config DTO
     *
     * @param permissionsAndRoleConfig role config that needs to be translated to role config DTO
     * @return permissionsAndRoleConfigDTO
     */
    public static PermissionsAndRoleConfigDTO createPermissionAndRoleConfigDTO(PermissionsAndRoleConfig
                                                                                       permissionsAndRoleConfig) {

        PermissionsAndRoleConfigDTO permissionsAndRoleConfigDTO = null;
        if (permissionsAndRoleConfig != null) {
            permissionsAndRoleConfigDTO = new PermissionsAndRoleConfigDTO();
            if (permissionsAndRoleConfig.getIdpRoles() != null) {
                permissionsAndRoleConfigDTO.setIdpRoles(Arrays.asList(permissionsAndRoleConfig.getIdpRoles()));
            }
            ApplicationPermission[] applicationPermissions = permissionsAndRoleConfig.getPermissions();
            if (applicationPermissions != null) {
                List<ApplicationPermissionDTO> applicationPermissionDTOs = new ArrayList<>();
                for (ApplicationPermission applicationPermission : applicationPermissions) {
                    ApplicationPermissionDTO applicationPermissionDTO = new ApplicationPermissionDTO();
                    applicationPermissionDTO.setValue(applicationPermission.getValue());
                    applicationPermissionDTOs.add(applicationPermissionDTO);
                }
                permissionsAndRoleConfigDTO.setPermissions(applicationPermissionDTOs);
            }
            RoleMapping[] roleMappings = permissionsAndRoleConfig.getRoleMappings();
            if (roleMappings != null) {
                List<RoleMappingDTO> roleMappingDTOs = new ArrayList<>();
                for (RoleMapping roleMapping : roleMappings) {
                    RoleMappingDTO roleMappingDTO = new RoleMappingDTO();
                    LocalRoleDTO localRoleDTO = new LocalRoleDTO();
                    localRoleDTO.setLocalRoleName(roleMapping.getLocalRole().getLocalRoleName());
                    localRoleDTO.setUserStoreId(roleMapping.getLocalRole().getUserStoreId());
                    roleMappingDTO.setLocalRole(localRoleDTO);
                    roleMappingDTO.setRemoteRole(roleMapping.getRemoteRole());
                    roleMappingDTOs.add(roleMappingDTO);
                }
                permissionsAndRoleConfigDTO.setRoleMappings(roleMappingDTOs);
            }
        }
        return permissionsAndRoleConfigDTO;
    }

    /**
     * Translate JIT provisioning config DTO to JIT provisioning config
     *
     * @param justInTimeProvisioningConfigDTO JIT provisioning config DTO that needs to be translated to JIT
     *                                        provisioning config
     * @return justInTimeProvisioningConfig object
     */
    public static JustInTimeProvisioningConfig createJustinTimeProvisioningConfig
    (JustInTimeProvisioningConfigDTO justInTimeProvisioningConfigDTO) {

        JustInTimeProvisioningConfig justInTimeProvisioningConfig = null;
        if (justInTimeProvisioningConfigDTO != null) {
            justInTimeProvisioningConfig = new JustInTimeProvisioningConfig();
            if (justInTimeProvisioningConfigDTO.getModifyUserNameAllowed() != null) {
                justInTimeProvisioningConfig.setModifyUserNameAllowed(justInTimeProvisioningConfigDTO
                        .getModifyUserNameAllowed());
            }
            if (justInTimeProvisioningConfigDTO.getPasswordProvisioningEnabled() != null) {
                justInTimeProvisioningConfig.setPasswordProvisioningEnabled(justInTimeProvisioningConfigDTO
                        .getPasswordProvisioningEnabled());
            }
            if (justInTimeProvisioningConfigDTO.getPromptConsent() != null) {
                justInTimeProvisioningConfig.setPromptConsent(justInTimeProvisioningConfigDTO.getPromptConsent());
            }
            justInTimeProvisioningConfig.setUserStoreClaimUri(justInTimeProvisioningConfigDTO.getUserStoreClaimUri());
        }
        return justInTimeProvisioningConfig;
    }

    /**
     * Translate JIT provisioning config to JIT provisioning config  DTO
     *
     * @param justInTimeProvisioningConfig JIT provisioning config that needs to be translated to JIT
     *                                     provisioning config DTO
     * @return justInTimeProvisioningConfigDTO object
     */
    public static JustInTimeProvisioningConfigDTO createJustinTimeProvisioningConfigDTO
    (JustInTimeProvisioningConfig justInTimeProvisioningConfig) {

        JustInTimeProvisioningConfigDTO justInTimeProvisioningConfigDTO = null;
        if (justInTimeProvisioningConfig != null) {
            justInTimeProvisioningConfigDTO = new JustInTimeProvisioningConfigDTO();
            justInTimeProvisioningConfigDTO.setModifyUserNameAllowed(justInTimeProvisioningConfig
                    .isModifyUserNameAllowed());
            justInTimeProvisioningConfigDTO.setPasswordProvisioningEnabled(justInTimeProvisioningConfig
                    .isPasswordProvisioningEnabled());
            justInTimeProvisioningConfigDTO.setPromptConsent(justInTimeProvisioningConfig.isPromptConsent());
            justInTimeProvisioningConfigDTO.setUserStoreClaimUri(justInTimeProvisioningConfig.getUserStoreClaimUri());
        }
        return justInTimeProvisioningConfigDTO;
    }

    private static List<IdentityProviderProperty> createIdentityProviderProperties(List<IdentityProviderPropertyDTO>
                                                                                           identityProviderPropertyDTOS) {

        List<IdentityProviderProperty> identityProviderProperties = new ArrayList<>();
        if (identityProviderPropertyDTOS != null) {
            identityProviderPropertyDTOS.forEach(identityProviderPropertyDTO -> {
                IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
                identityProviderProperty.setDisplayName(identityProviderPropertyDTO.getDisplayName());
                identityProviderProperty.setName(identityProviderPropertyDTO.getName());
                identityProviderProperty.setValue(identityProviderPropertyDTO.getValue());
                identityProviderProperties.add(identityProviderProperty);
            });
        }
        return identityProviderProperties;
    }

    private static List<IdentityProviderPropertyDTO> createIdentityProviderDTOProperties
            (List<IdentityProviderProperty> identityProviderProperties) {

        List<IdentityProviderPropertyDTO> identityProviderDTOProperties = new ArrayList<>();
        if (identityProviderProperties != null) {
            identityProviderProperties.forEach(identityProviderProperty -> {
                IdentityProviderPropertyDTO identityProviderPropertyDTO = new IdentityProviderPropertyDTO();
                identityProviderPropertyDTO.setDisplayName(identityProviderProperty.getDisplayName());
                identityProviderPropertyDTO.setName(identityProviderProperty.getName());
                identityProviderPropertyDTO.setValue(identityProviderProperty.getValue());
                identityProviderDTOProperties.add(identityProviderPropertyDTO);
            });
        }
        return identityProviderDTOProperties;
    }

    private static List<FederatedAuthenticatorConfig> createFederatorAuthenticatorList
            (List<FederatedAuthenticatorConfigDTO> federatedAuthenticatorConfigDTOS) {

        List<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs = new ArrayList<>();
        if (federatedAuthenticatorConfigDTOS != null) {
            for (FederatedAuthenticatorConfigDTO federatedAuthenticatorConfigDTO : federatedAuthenticatorConfigDTOS) {
                federatedAuthenticatorConfigs.add(createDefaultAuthenticator(federatedAuthenticatorConfigDTO));
            }
        }
        return federatedAuthenticatorConfigs;
    }

    /**
     * Translate federated authenticator config to federated authenticator config DTO list
     *
     * @param federatedAuthenticatorConfigs federated authenticator config list that needs to be translated to
     *                                      federated authenticator config DTO list
     * @return federatedAuthenticatorConfigDTOs list
     */
    public static List<FederatedAuthenticatorConfigDTO> createFederatorAuthenticatorDTOList
    (List<FederatedAuthenticatorConfig> federatedAuthenticatorConfigs) {

        List<FederatedAuthenticatorConfigDTO> federatedAuthenticatorConfigDTOs = new ArrayList<>();
        if (federatedAuthenticatorConfigs != null) {
            for (FederatedAuthenticatorConfig federatedAuthenticatorConfig : federatedAuthenticatorConfigs) {
                federatedAuthenticatorConfigDTOs.add(createDefaultAuthenticatorDTO(federatedAuthenticatorConfig));
            }
        }
        return federatedAuthenticatorConfigDTOs;
    }

    /**
     * Translate provision Connector Configuration DTO to provision Connector Configuration
     *
     * @param provisioningConnectorConfigDTO provision Connector Configuration DTO that needs to be translated to
     *                                       provision Connector Configuration
     * @return provisioningConnectorConfig
     */
    public static ProvisioningConnectorConfig createProvisioningConnectorConfig(ProvisioningConnectorConfigDTO
                                                                                        provisioningConnectorConfigDTO) {

        ProvisioningConnectorConfig provisioningConnectorConfig = null;
        if (provisioningConnectorConfigDTO != null) {
            provisioningConnectorConfig = new ProvisioningConnectorConfig();
            if (provisioningConnectorConfigDTO.getBlocking() != null) {
                provisioningConnectorConfig.setBlocking(provisioningConnectorConfigDTO.getBlocking());
            }
            provisioningConnectorConfig.setName(provisioningConnectorConfigDTO.getName());
            if (provisioningConnectorConfigDTO.getEnabled() != null) {
                provisioningConnectorConfig.setEnabled(provisioningConnectorConfigDTO.getEnabled());
            }
            List<Property> propertyList = getPropertyList(provisioningConnectorConfigDTO.getProvisioningProperties());
            provisioningConnectorConfig.setProvisioningProperties(propertyList.toArray(new Property[0]));
            if (provisioningConnectorConfigDTO.getRulesEnabled() != null) {
                provisioningConnectorConfig.setRulesEnabled(provisioningConnectorConfigDTO.getRulesEnabled());
            }
        }
        return provisioningConnectorConfig;
    }

    private static ProvisioningConnectorConfigDTO createProvisioningConnectorConfigDTO(ProvisioningConnectorConfig
                                                                                               provisioningConnectorConfig) {

        ProvisioningConnectorConfigDTO provisioningConnectorConfigDTO = null;
        if (provisioningConnectorConfig != null) {
            provisioningConnectorConfigDTO = new ProvisioningConnectorConfigDTO();
            provisioningConnectorConfigDTO.setBlocking(provisioningConnectorConfig.isBlocking());
            provisioningConnectorConfigDTO.setName(provisioningConnectorConfig.getName());
            provisioningConnectorConfigDTO.setEnabled(provisioningConnectorConfig.isEnabled());
            provisioningConnectorConfigDTO.setValid(provisioningConnectorConfig.isValid());
            provisioningConnectorConfigDTO.setRulesEnabled(provisioningConnectorConfig.isRulesEnabled());
            if (ArrayUtils.isNotEmpty(provisioningConnectorConfig.getProvisioningProperties())) {
                List<PropertyDTO> propertyListDTO = getPropertyListDTO(Arrays.asList(provisioningConnectorConfig
                        .getProvisioningProperties()));
                provisioningConnectorConfigDTO.setProvisioningProperties(propertyListDTO);
            }
            provisioningConnectorConfigDTO.setRulesEnabled(provisioningConnectorConfigDTO.getRulesEnabled());
        }
        return provisioningConnectorConfigDTO;
    }

    /**
     * Translate Claim Configuration DTO to Claim Configuration
     *
     * @param claimConfigDTO Claim Configuration DTO that needs to be translated to
     *                       Claim Configuration
     * @return claimConfig
     */
    public static ClaimConfig createClaimConfig(ClaimConfigDTO claimConfigDTO) {

        ClaimConfig claimConfig = null;
        if (claimConfigDTO != null) {
            claimConfig = new ClaimConfig();
            claimConfig.setRoleClaimURI(claimConfigDTO.getRoleClaimURI());
            if (claimConfigDTO.getAlwaysSendMappedLocalSubjectId() != null) {
                claimConfig.setAlwaysSendMappedLocalSubjectId(claimConfigDTO.getAlwaysSendMappedLocalSubjectId());
            }
            if (claimConfigDTO.getLocalClaimDialect() != null) {
                claimConfig.setLocalClaimDialect(claimConfigDTO.getLocalClaimDialect());
            }
            claimConfig.setUserClaimURI(claimConfigDTO.getUserClaimURI());
            if (claimConfigDTO.getClaimMappings() != null) {
                List<ClaimMapping> claimMappings = new ArrayList<>();
                for (ClaimMappingDTO claimMappingDTO : claimConfigDTO.getClaimMappings()) {
                    ClaimMapping claimMapping = new ClaimMapping();
                    claimMapping.setDefaultValue(claimMappingDTO.getDefaultValue());
                    claimMapping.setLocalClaim(createClaim(claimMappingDTO.getLocalClaim()));
                    if (claimMappingDTO.getMandatory() != null) {
                        claimMapping.setMandatory(claimMappingDTO.getMandatory());
                    }
                    if (claimMappingDTO.getRequired() != null) {
                        claimMapping.setRequested(claimMappingDTO.getRequired());
                    }
                    claimMapping.setRemoteClaim(createClaim(claimMappingDTO.getRemoteClaim()));
                    claimMappings.add(claimMapping);
                }
                claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[0]));
            }
            if (claimConfigDTO.getSpClaimDialects() != null) {
                claimConfig.setSpClaimDialects(claimConfigDTO.getSpClaimDialects().toArray(new String[0]));
            }
            if (claimConfigDTO.getIdpClaims() != null) {
                List<Claim> claimConfigs = new ArrayList<>();
                for (ClaimDTO claimDTO : claimConfigDTO.getIdpClaims()) {
                    Claim claim = createClaim(claimDTO);
                    claimConfigs.add(claim);
                }
                claimConfig.setIdpClaims(claimConfigs.toArray(new Claim[0]));
            }
        }
        return claimConfig;
    }

    private static Claim createClaim(ClaimDTO claimDTO) {

        Claim claim = null;
        if (claimDTO != null) {
            claim = new Claim();
            if (claimDTO.getClaimId() != null) {
                claim.setClaimId(claimDTO.getClaimId());
            }
            claim.setClaimUri(claimDTO.getClaimUri());
        }
        return claim;
    }

    private static ClaimDTO createClaimDTO(Claim claim) {

        ClaimDTO claimDTO = null;
        if (claim != null) {
            claimDTO = new ClaimDTO();
            claimDTO.setClaimId(claim.getClaimId());
            claimDTO.setClaimUri(claim.getClaimUri());
        }
        return claimDTO;
    }

    /**
     * Translate Claim Configuration to Claim Configuration DTO
     *
     * @param claimConfig Claim Configuration  that needs to be translated to Claim Configuration DTO
     * @return claimConfigDTO
     */
    public static ClaimConfigDTO createClaimConfigDTO(ClaimConfig claimConfig) {

        ClaimConfigDTO claimConfigDTO = null;

        if (claimConfig != null) {
            claimConfigDTO = new ClaimConfigDTO();
            claimConfigDTO.setRoleClaimURI(claimConfig.getRoleClaimURI());
            claimConfigDTO.setAlwaysSendMappedLocalSubjectId(claimConfig.isAlwaysSendMappedLocalSubjectId());
            claimConfigDTO.setLocalClaimDialect(claimConfig.isLocalClaimDialect());
            claimConfigDTO.setUserClaimURI(claimConfig.getUserClaimURI());
            if (claimConfig.getClaimMappings() != null) {
                List<ClaimMappingDTO> claimMappingDTOs = new ArrayList<>();
                for (ClaimMapping claimMapping : claimConfig.getClaimMappings()) {
                    ClaimMappingDTO claimMappingDTO = new ClaimMappingDTO();
                    claimMappingDTO.setDefaultValue(claimMapping.getDefaultValue());
                    claimMappingDTO.setLocalClaim(createClaimDTO(claimMapping.getLocalClaim()));
                    claimMappingDTO.setMandatory(claimMapping.isMandatory());
                    claimMappingDTO.setRequired(claimMapping.isRequested());
                    claimMappingDTO.setRemoteClaim(createClaimDTO(claimMapping.getRemoteClaim()));
                    claimMappingDTOs.add(claimMappingDTO);
                }
                claimConfigDTO.setClaimMappings(claimMappingDTOs);
            }
            if (claimConfig.getSpClaimDialects() != null) {
                claimConfigDTO.setSpClaimDialects(Arrays.asList(claimConfig.getSpClaimDialects()));
            }
            if (claimConfig.getIdpClaims() != null) {
                ArrayList<Claim> claims = new ArrayList<>(Arrays.asList(claimConfig.getIdpClaims()));
                List<ClaimDTO> claimConfigDTOS = new ArrayList<>();
                for (Claim claim : claims) {
                    ClaimDTO claimDTO = new ClaimDTO();
                    claimDTO.setClaimId(claim.getClaimId());
                    claimDTO.setClaimUri(claim.getClaimUri());
                    claimConfigDTOS.add(claimDTO);
                }
                claimConfigDTO.setIdpClaims(claimConfigDTOS);
            }
        }
        return claimConfigDTO;
    }

    /**
     * Translate Federated authenticator configuration DTO to federated authenticator configuration
     *
     * @param federatedAuthenticatorConfigDTO federated authenticator DTO that needs to be translated to federated
     *                                        authenticator
     * @return federatedAuthenticatorConfig
     */
    public static FederatedAuthenticatorConfig createDefaultAuthenticator(FederatedAuthenticatorConfigDTO
                                                                                  federatedAuthenticatorConfigDTO) {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = null;
        if (federatedAuthenticatorConfigDTO != null) {
            federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
            federatedAuthenticatorConfig.setDisplayName(federatedAuthenticatorConfigDTO.getDisplayName());
            if (federatedAuthenticatorConfigDTO.getEnabled() != null) {
                federatedAuthenticatorConfig.setEnabled(federatedAuthenticatorConfigDTO.getEnabled());
            }
            federatedAuthenticatorConfig.setName(federatedAuthenticatorConfigDTO.getName());

            List<Property> propertyList = getPropertyList(federatedAuthenticatorConfigDTO.getPropertyList());
            federatedAuthenticatorConfig.setProperties(propertyList.toArray(new Property[0]));
        }
        return federatedAuthenticatorConfig;
    }

    public static FederatedAuthenticatorConfigDTO createDefaultAuthenticatorDTO(FederatedAuthenticatorConfig
                                                                                        federatedAuthenticatorConfig) {

        FederatedAuthenticatorConfigDTO federatedAuthenticatorConfigDTO = null;
        if (federatedAuthenticatorConfig != null) {
            federatedAuthenticatorConfigDTO = new FederatedAuthenticatorConfigDTO();
            federatedAuthenticatorConfigDTO.setDisplayName(federatedAuthenticatorConfig.getDisplayName());
            federatedAuthenticatorConfigDTO.setEnabled(federatedAuthenticatorConfig.isEnabled());
            federatedAuthenticatorConfigDTO.setName(federatedAuthenticatorConfig.getName());

            if (ArrayUtils.isNotEmpty(federatedAuthenticatorConfig.getProperties())) {
                List<PropertyDTO> propertyList = getPropertyListDTO(Arrays.asList(federatedAuthenticatorConfig.getProperties()));
                federatedAuthenticatorConfigDTO.setPropertyList(propertyList);
            }
        }
        return federatedAuthenticatorConfigDTO;
    }

    private static List<PropertyDTO> getPropertyListDTO(List<Property> properties) {

        List<PropertyDTO> propertyList = new ArrayList<>();
        if (properties != null) {
            properties.forEach(property -> {
                PropertyDTO propertyDTO = new PropertyDTO();
                propertyDTO.setValue(property.getValue());
                propertyDTO.setAdvanced(property.isAdvanced());
                propertyDTO.setConfidential(property.isConfidential());
                propertyDTO.setDefaultValue(property.getDefaultValue());
                propertyDTO.setDisplayName(property.getDescription());
                propertyDTO.setDescription(property.getDisplayName());
                propertyDTO.setDisplayOrder(property.getDisplayOrder());
                propertyDTO.setName(property.getName());
                propertyDTO.setRequired(property.isRequired());
                propertyDTO.setType(property.getType());
                propertyDTO.setValue(property.getValue());
                propertyList.add(propertyDTO);
            });
        }

        return propertyList;
    }

    private static List<Property> getPropertyList(List<PropertyDTO> propertyDTOS) {

        List<Property> propertyList = new ArrayList<>();
        if (propertyDTOS != null) {
            propertyDTOS.forEach(propertyDTO -> {
                Property property = new Property();

                property.setValue(propertyDTO.getValue());
                if (propertyDTO.getAdvanced() != null) {
                    property.setAdvanced(propertyDTO.getAdvanced());
                }
                if (propertyDTO.getConfidential() != null) {
                    property.setConfidential(propertyDTO.getConfidential());
                }
                property.setDefaultValue(propertyDTO.getDefaultValue());
                property.setDisplayName(propertyDTO.getDescription());
                property.setDescription(propertyDTO.getDisplayName());
                if (propertyDTO.getDisplayOrder() != null) {
                    property.setDisplayOrder(propertyDTO.getDisplayOrder());
                }
                property.setName(propertyDTO.getName());
                if (propertyDTO.getRequired() != null) {
                    property.setRequired(propertyDTO.getRequired());
                }
                property.setType(propertyDTO.getType());
                property.setValue(propertyDTO.getValue());
                propertyList.add(property);
            });
        }

        return propertyList;
    }

    /**
     * Handles InternalServerErrorExceptions
     *
     * @param errorCode error code
     * @param log       log object
     * @param exception exception that needs to be passed
     * @return InternalServerErrorException object
     */
    public static InternalServerErrorException buildInternalServerErrorException(String errorCode,
                                                                                 Log log,
                                                                                 Exception exception) {

        ErrorDTO errorDTO = getErrorDTO(IdPConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT,
                IdPConstants.STATUS_INTERNAL_SERVER_ERROR_MESSAGE_DEFAULT, errorCode);
        logError(log, exception);
        return new InternalServerErrorException(errorDTO);
    }

    /**
     * Handles BadRequestException
     *
     * @param description error code
     * @param log         log object
     * @param code        error code
     * @return BadRequestException object
     */
    public static BadRequestException buildBadRequestException(String code, String description,
                                                               Log log, IDPMgtBridgeServiceClientException e) {

        ErrorDTO errorDTO = getErrorDTO(IdPConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, description, code);
        logDebug(log, e);
        return new BadRequestException(errorDTO);
    }

    private static void logDebug(Log log, Throwable throwable) {

        if (log.isDebugEnabled()) {
            log.debug(IdPConstants.STATUS_BAD_REQUEST_MESSAGE_DEFAULT, throwable);
        }
    }

    private static void logError(Log log, Throwable throwable) {

        log.error(throwable.getMessage(), throwable);
    }

    private static ErrorDTO getErrorDTO(String message, String description, String code) {

        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setDescription(description);
        return errorDTO;
    }
}

/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Authentication configuration of an application.
 */
public class ApplicationConfig implements Serializable, Cloneable {

    private static final long serialVersionUID = 8082478632322393384L;
    private static final Log log = LogFactory.getLog(ApplicationConfig.class);

    private String serviceProviderResourceId;

    private final String tenantDomain;
    private boolean mappedSubjectIDSelected = false;
    private final List<OptimizedAuthStep> optimizedAuthSteps;

    private Map<String, String> claimMappings = new HashMap<>();
    private Map<String, String> roleMappings = new HashMap<>();
    private Map<String, String> requestedClaims = new HashMap<>();
    private Map<String, String> mandatoryClaims = new HashMap<>();

    private class OptimizedAuthStep implements Serializable {

        private final int stepOrder;
        private final List<String> localAuthenticatorConfigNames;
        private final List<String> federatedIdPResourceIds;
        private final boolean subjectStep;
        private final boolean attributeStep;

        private OptimizedAuthStep(AuthenticationStep authenticationStep) throws FrameworkException {

            this.stepOrder = authenticationStep.getStepOrder();
            this.localAuthenticatorConfigNames =
                    setLocalAuthenticatorConfigNames(authenticationStep.getLocalAuthenticatorConfigs());
            this.federatedIdPResourceIds =
                    setFederatedIdpResourceId(authenticationStep.getFederatedIdentityProviders());
            this.subjectStep = authenticationStep.isSubjectStep();
            this.attributeStep = authenticationStep.isAttributeStep();
        }

        private List<String> setLocalAuthenticatorConfigNames(LocalAuthenticatorConfig[] localAuthenticatorConfigs) {

            List<String> configNames = new ArrayList<>();
            for (LocalAuthenticatorConfig config : localAuthenticatorConfigs) {
                configNames.add(config.getName());
            }
            return configNames;
        }

        private LocalAuthenticatorConfig[] getLocalAuthenticatorConfigs() {
            ApplicationAuthenticatorService authenticatorService = ApplicationAuthenticatorService.getInstance();
            LocalAuthenticatorConfig[] localAuthenticatorConfigs =
                    new LocalAuthenticatorConfig[this.localAuthenticatorConfigNames.size()];
            for (int i = 0; i < this.localAuthenticatorConfigNames.size(); i++) {
                localAuthenticatorConfigs[i] = authenticatorService.
                        getLocalAuthenticatorByName(this.localAuthenticatorConfigNames.get(i));
            }
            return localAuthenticatorConfigs;
        }

        private List<String> setFederatedIdpResourceId(IdentityProvider[] idpList) throws FrameworkException {
            List<String> resourceIdList = new ArrayList<>();
            IdentityProviderManager manager = IdentityProviderManager.getInstance();
            if (idpList != null) {
                for (IdentityProvider idp : idpList) {
                    try {
                        idp = manager.getIdPByName(idp.getIdentityProviderName(), getTenantDomain());
                        resourceIdList.add(idp.getResourceId());
                    } catch (IdentityProviderManagementException e) {
                        throw new FrameworkException("Failed to get the IdP by Name: " + idp.getIdentityProviderName()
                                + " TenantDomain: " + getTenantDomain(), e);
                    }
                }
            }
            return resourceIdList;
        }

        private IdentityProvider[] getFederatedIdentityProviders() throws FrameworkException {
            IdentityProviderManager manager = IdentityProviderManager.getInstance();
            IdentityProvider[] federatedIdP = new IdentityProvider[this.federatedIdPResourceIds.size()];
            for (int i = 0; i < this.federatedIdPResourceIds.size(); i++) {
                try {
                    federatedIdP[i] = manager.getIdPByResourceId(this.federatedIdPResourceIds.get(i), getTenantDomain()
                            , false);
                } catch (IdentityProviderManagementException e) {
                    throw new FrameworkException("Failed to get the IdP by the Resource ID: " +
                            this.federatedIdPResourceIds.get(i) + " Tenant Domain: " + getTenantDomain(), e);
                }
            }
            return federatedIdP;
        }

        private AuthenticationStep getAuthenticationStep() throws FrameworkException {

            AuthenticationStep authenticationStep = new AuthenticationStep();
            authenticationStep.setStepOrder(this.stepOrder);
            authenticationStep.setLocalAuthenticatorConfigs(getLocalAuthenticatorConfigs());
            authenticationStep.setFederatedIdentityProviders(getFederatedIdentityProviders());
            authenticationStep.setSubjectStep(this.subjectStep);
            authenticationStep.setAttributeStep(this.attributeStep);
            return authenticationStep;
        }
    }


    public ApplicationConfig(ServiceProvider application, String tenantDomain) throws FrameworkException {

        this.serviceProviderResourceId = application.getApplicationResourceId();
        this.tenantDomain = tenantDomain;

        ClaimConfig claimConfig = application.getClaimConfig();
        if (claimConfig != null) {

            List<ClaimMapping> spClaimMappings = new ArrayList<>(Arrays.asList(claimConfig.getClaimMappings()));
            setSpDialectClaims(claimConfig, spClaimMappings);
            if (CollectionUtils.isNotEmpty(spClaimMappings)) {
                for (ClaimMapping claim : spClaimMappings) {
                    if (claim.getRemoteClaim() != null
                        && claim.getRemoteClaim().getClaimUri() != null) {
                        if (claim.getLocalClaim() != null) {
                            claimMappings.put(claim.getRemoteClaim().getClaimUri(), claim
                                    .getLocalClaim().getClaimUri());

                            if (claim.isRequested()) {
                                requestedClaims.put(claim.getRemoteClaim().getClaimUri(), claim
                                        .getLocalClaim().getClaimUri());
                            }

                            if (claim.isMandatory()) {
                                mandatoryClaims.put(claim.getRemoteClaim().getClaimUri(), claim
                                        .getLocalClaim().getClaimUri());
                            }

                        } else {
                            claimMappings.put(claim.getRemoteClaim().getClaimUri(), null);
                            if (claim.isRequested()) {
                                requestedClaims.put(claim.getRemoteClaim().getClaimUri(), null);
                            }

                            if (claim.isMandatory()) {
                                mandatoryClaims.put(claim.getRemoteClaim().getClaimUri(), null);
                            }
                        }
                    }

                }
            }
        }

        LocalAndOutboundAuthenticationConfig outboundAuthenticationConfig =
                application.getLocalAndOutBoundAuthenticationConfig();
        AuthenticationStep[] authenticationSteps = new AuthenticationStep[0];
        if (outboundAuthenticationConfig != null) {
            authenticationSteps = outboundAuthenticationConfig.getAuthenticationSteps();
        }
        List<OptimizedAuthStep> optimizedAuthSteps = new ArrayList<>();
        for (AuthenticationStep step : authenticationSteps) {
            OptimizedAuthStep optimizedAuthStep = new OptimizedAuthStep(step);
            optimizedAuthSteps.add(optimizedAuthStep);
        }
        this.optimizedAuthSteps = optimizedAuthSteps;

        PermissionsAndRoleConfig permissionRoleConfiguration;
        permissionRoleConfiguration = application.getPermissionAndRoleConfig();

        if (permissionRoleConfiguration != null) {

            RoleMapping[] tempRoleMappings = permissionRoleConfiguration.getRoleMappings();
            if (tempRoleMappings != null && tempRoleMappings.length > 0) {
                for (RoleMapping roleMapping : tempRoleMappings) {
                    this.roleMappings.put(roleMapping.getLocalRole().getLocalRoleName(),
                                          roleMapping.getRemoteRole());
                }
            }
        }
    }

    @Deprecated
    public void setUseUserstoreDomainInRole(boolean useUserstoreDomainInRole) {
        log.warn(String.format("Cannot set useUserStoreDomainInRoles: %s " +
                "after the optimization of the service provider", useUserstoreDomainInRole));
    }

    public int getApplicationID() throws FrameworkException {

        return getServiceProvider().getApplicationID();
    }

    public String getTenantDomain() {

        return this.tenantDomain;
    }

    @Deprecated
    public void setApplicationID(int applicationID) {

        log.warn(String.format("Cannot set application ID: %s " +
                "after the optimization of the service provider", applicationID));
    }

    public String getApplicationName() throws FrameworkException {

        return getServiceProvider().getApplicationName();
    }

    @Deprecated
    public void setApplicationName(String applicationName) {

        log.warn(String.format("Cannot set application name: %s " +
                "after the optimization of the service provider", applicationName));
    }

    public String getRoleClaim() throws FrameworkException {

        String roleClaim = null;
        ServiceProvider serviceProvider = getServiceProvider();
        if (serviceProvider.getClaimConfig() != null) {
            roleClaim = serviceProvider.getClaimConfig().getRoleClaimURI();
        }
        return roleClaim;
    }

    @Deprecated
    public void setRoleClaim(String roleClaim) {

        log.warn(String.format("Cannot set roleClaim: %s " +
                "after the optimization of the service provider", roleClaim));
    }

    public String[] getPermissions() throws FrameworkException {

        String[] permissions = new String[0];
        PermissionsAndRoleConfig permissionRoleConfiguration;
        permissionRoleConfiguration = getServiceProvider().getPermissionAndRoleConfig();

        if (permissionRoleConfiguration != null) {
            ApplicationPermission[] permissionList = permissionRoleConfiguration.getPermissions();
            if (permissionList == null) {
                permissionList = new ApplicationPermission[0];
            }

            permissions = new String[permissionList.length];

            for (int i = 0; i < permissionList.length; i++) {
                ApplicationPermission permission = permissionList[i];
                permissions[i] = permission.getValue();
            }
        }
        return permissions.clone();
    }

    @Deprecated
    public void setPermissions(String[] permissions) {

        log.warn("Cannot set permissions: after the optimization of the service provider");
    }

    public Map<String, String> getClaimMappings() {

        return this.claimMappings;
    }

    public void setClaimMappings(Map<String, String> claimMappings) {

        this.claimMappings = claimMappings;
    }

    public Map<String, String> getRequestedClaimMappings() {

        return this.requestedClaims;
    }

    /**
     * Set application requested claims.
     *
     * @param requestedClaims requested claims
     */
    public void setRequestedClaims(Map<String, String> requestedClaims) {

        this.requestedClaims = requestedClaims;
    }

    public Map<String, String> getMandatoryClaimMappings() {

        return this.mandatoryClaims;
    }

    /**
     * Get application requested mandatory claims.
     *
     * @param mandatoryClaims mandatory claims
     */
    public void setMandatoryClaims(Map<String, String> mandatoryClaims) {

        this.mandatoryClaims = mandatoryClaims;
    }

    public Map<String, String> getRoleMappings() {

        return this.roleMappings;
    }

    public void setRoleMappings(Map<String, String> roleMappings) {

        this.roleMappings = roleMappings;
    }

    @Deprecated
    public boolean noClaimMapping() {

        return claimMappings == null;
    }

    public boolean isAlwaysSendMappedLocalSubjectId() throws FrameworkException {

        boolean alwaysSendMappedLocalSubjectId = false;
        ServiceProvider serviceProvider = getServiceProvider();
        if (serviceProvider.getClaimConfig() != null) {
            alwaysSendMappedLocalSubjectId = serviceProvider.getClaimConfig().isAlwaysSendMappedLocalSubjectId();
        }
        return alwaysSendMappedLocalSubjectId;
    }

    @Deprecated
    public void setAlwaysSendMappedLocalSubjectId(boolean alwaysSendMappedLocalSubjectId) {

        log.warn(String.format("Cannot set alwaysSendMappedLocalSubjectId: %s " +
                "after the optimization of the service provider", alwaysSendMappedLocalSubjectId));
    }

    public boolean isMappedSubjectIDSelected() {

        return mappedSubjectIDSelected;
    }

    public void setMappedSubjectIDSelected(boolean mappedSubjectIDSelected) {

        this.mappedSubjectIDSelected = mappedSubjectIDSelected;
    }

    public String getSubjectClaimUri() throws FrameworkException {

        String subjectClaimUri = null;
        if (getServiceProvider().getLocalAndOutBoundAuthenticationConfig() != null) {
            subjectClaimUri = getServiceProvider().getLocalAndOutBoundAuthenticationConfig().getSubjectClaimUri();
        }
        return subjectClaimUri;
    }

    public ServiceProvider getServiceProvider() throws FrameworkException {

        return reconstructServiceProvider();
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {

        this.serviceProviderResourceId = serviceProvider.getApplicationResourceId();
    }

    public boolean isSaaSApp() throws FrameworkException {
        return getServiceProvider().isSaasApp();
    }

    public boolean isUseTenantDomainInLocalSubjectIdentifier() throws FrameworkException {

        boolean useTenantDomainInLocalSubjectIdentifier = false;
        LocalAndOutboundAuthenticationConfig outboundConfig =
                getServiceProvider().getLocalAndOutBoundAuthenticationConfig();
        if (outboundConfig != null) {
            useTenantDomainInLocalSubjectIdentifier = outboundConfig.isUseTenantDomainInLocalSubjectIdentifier();
        }
        return useTenantDomainInLocalSubjectIdentifier;
    }

    @Deprecated
    public void setUseTenantDomainInLocalSubjectIdentifier(boolean useTenantDomainInLocalSubjectIdentifier) {

        log.warn(String.format("Cannot set useTenantDomainInLocalSubjectIdentifier: %s " +
                "after the optimization of the service provider", useTenantDomainInLocalSubjectIdentifier));
    }

    public boolean isUseUserstoreDomainInLocalSubjectIdentifier() throws FrameworkException {

        LocalAndOutboundAuthenticationConfig outboundConfig =
                getServiceProvider().getLocalAndOutBoundAuthenticationConfig();
        boolean useUserstoreDomainInLocalSubjectIdentifier = false;
        if (outboundConfig != null) {
            useUserstoreDomainInLocalSubjectIdentifier = outboundConfig.isUseUserstoreDomainInLocalSubjectIdentifier();
        }
        return useUserstoreDomainInLocalSubjectIdentifier;
    }

    @Deprecated
    public void setUseUserstoreDomainInLocalSubjectIdentifier(boolean useUserstoreDomainInLocalSubjectIdentifier) {

        log.warn(String.format("Cannot set useUserStoreDomainInLocalSubjectIdentifier: %s " +
                "after the optimization of the service provider", useUserstoreDomainInLocalSubjectIdentifier));
    }

    public boolean isEnableAuthorization() throws FrameworkException {

        LocalAndOutboundAuthenticationConfig outboundAuthenticationConfig =
                getServiceProvider().getLocalAndOutBoundAuthenticationConfig();
        boolean enableAuthorization = false;
        if (outboundAuthenticationConfig != null) {
            enableAuthorization = outboundAuthenticationConfig.isEnableAuthorization();
        }
        return enableAuthorization;
    }

    @Deprecated
    public void setEnableAuthorization(boolean enableAuthorization) {

        log.warn(String.format("Cannot set enableAuthorization: %s " +
                "after the optimization of the service provider", enableAuthorization));
    }

    /**
     * This method will clone current class objects.
     * This method is to solve the issue - multiple requests for same user/SP
     *
     * @return Object object
     */
    public Object clone() throws CloneNotSupportedException {

        ApplicationConfig applicationConfig = (ApplicationConfig) super.clone();
        applicationConfig.setClaimMappings(new HashMap<>(this.claimMappings));
        applicationConfig.setRoleMappings(new HashMap<>(this.roleMappings));
        applicationConfig.requestedClaims = new HashMap<>(this.requestedClaims);
        applicationConfig.mandatoryClaims = new HashMap<>(this.mandatoryClaims);
        return applicationConfig;
    }

    /**
     * Set all the claim mappings of the configured SP claim dialects.
     *
     * @param claimConfig Application claim configuration
     * return Application claim mappings
     */
    private void setSpDialectClaims(ClaimConfig claimConfig, List<ClaimMapping> spClaimMappings) {

        String[] spClaimDialects = claimConfig.getSpClaimDialects();
        if (!ArrayUtils.isEmpty(spClaimDialects)) {
            List<String> spClaimDialectsList = Arrays.asList(spClaimDialects);
            spClaimDialectsList.forEach(spClaimDialect -> {
                try {
                    String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
                    if (ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT.equals(spClaimDialect)) {
                        List<LocalClaim> localClaims = FrameworkServiceDataHolder.getInstance()
                                .getClaimMetadataManagementService().getLocalClaims(tenantDomain);
                        localClaims.stream().map(localClaim -> ClaimMapping.build(localClaim
                                .getClaimURI(), localClaim.getClaimURI(), null, true))
                                .forEach(spClaimMappings::add);
                    } else {
                        List<ExternalClaim> externalClaims = FrameworkServiceDataHolder.getInstance()
                                .getClaimMetadataManagementService().getExternalClaims(spClaimDialect, tenantDomain);
                        externalClaims.stream().map(externalClaim -> ClaimMapping.build(externalClaim
                                .getMappedLocalClaim(), externalClaim.getClaimURI(), null, true))
                                .forEach(spClaimMappings::add);
                    }
                } catch (ClaimMetadataException e) {
                    log.error("Error when getting external claims of dialect: " + spClaimDialect, e);
                }
            });
        }
    }

    public boolean isUseUserIdForDefaultSubject() throws FrameworkException {

        boolean useUserIdForDefaultSubject = false;
        ServiceProviderProperty[] spProperties = getServiceProvider().getSpProperties();
        if (spProperties != null) {
            for (ServiceProviderProperty prop: spProperties) {
                if (IdentityApplicationConstants.USE_USER_ID_FOR_DEFAULT_SUBJECT.equals(prop.getName())) {
                    useUserIdForDefaultSubject = Boolean.parseBoolean(prop.getValue());
                    break;
                }
            }
        }
        return useUserIdForDefaultSubject;
    }

    private ServiceProvider reconstructServiceProvider() throws FrameworkException {

        ServiceProvider serviceProvider;
        try {
            serviceProvider = ApplicationMgtSystemConfig.getInstance().getApplicationDAO()
                    .getApplicationByResourceId(this.serviceProviderResourceId, this.tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            throw new FrameworkException(String.format("Failed to get the application with ID: %s in tenant domain: %s"
                    , this.serviceProviderResourceId, this.tenantDomain), e);
        }
        if (serviceProvider == null) {
            return null;
        }
        AuthenticationStep[] authenticationSteps = new AuthenticationStep[this.optimizedAuthSteps.size()];
        for (int i = 0; i < this.optimizedAuthSteps.size(); i++) {
            authenticationSteps[i] = optimizedAuthSteps.get(i).getAuthenticationStep();
        }
        serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(authenticationSteps);
        return serviceProvider;
    }

}

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
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

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

    private ServiceProvider serviceProvider = null;

    private int applicationID = 0;
    private String applicationName = null;
    private String roleClaim = null;
    private boolean alwaysSendMappedLocalSubjectId = false;
    private boolean mappedSubjectIDSelected = false;
    private String subjectClaimUri;
    private String[] permissions = new String[0];
    private Map<String, String> claimMappings = new HashMap<>();
    private Map<String, String> roleMappings = new HashMap<>();
    private Map<String, String> requestedClaims = new HashMap<>();
    private Map<String, String> mandatoryClaims = new HashMap<>();
    private boolean isSaaSApp;
    private boolean useTenantDomainInLocalSubjectIdentifier = false;
    private boolean useUserstoreDomainInLocalSubjectIdentifier = false;
    private boolean enableAuthorization = false;
    private boolean useUserstoreDomainInRole = false;
    private boolean useUserIdForDefaultSubject = false;

    private static final Log log = LogFactory.getLog(ApplicationConfig.class);

    /**
     * @param application Application details.
     * @deprecated Use {@link #ApplicationConfig(ServiceProvider, String)} instead.
     */
    @Deprecated
    public ApplicationConfig(ServiceProvider application) {

        this(application, PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
    }

    /**
     * Initialize application (service provider) configurations.
     *
     * @param application Application details.
     * @param tenantDomain Tenant domain of the application.
     */
    public ApplicationConfig(ServiceProvider application, String tenantDomain) {

        this.serviceProvider = application;
        applicationID = application.getApplicationID();
        applicationName = application.getApplicationName();
        isSaaSApp = application.isSaasApp();
        LocalAndOutboundAuthenticationConfig outboundAuthConfig = application.getLocalAndOutBoundAuthenticationConfig();

        if (outboundAuthConfig != null) {
            subjectClaimUri = outboundAuthConfig.getSubjectClaimUri();
            setUseTenantDomainInLocalSubjectIdentifier(outboundAuthConfig.isUseTenantDomainInLocalSubjectIdentifier());
            setUseUserstoreDomainInLocalSubjectIdentifier(outboundAuthConfig
                    .isUseUserstoreDomainInLocalSubjectIdentifier());
            setEnableAuthorization(outboundAuthConfig.isEnableAuthorization());
            setUseUserstoreDomainInRole(outboundAuthConfig.isUseUserstoreDomainInRoles());
        }


        ClaimConfig claimConfig = application.getClaimConfig();
        if (claimConfig != null) {
            roleClaim = claimConfig.getRoleClaimURI();
            alwaysSendMappedLocalSubjectId = claimConfig.isAlwaysSendMappedLocalSubjectId();

            List<ClaimMapping> spClaimMappings = new ArrayList<>(Arrays.asList(claimConfig.getClaimMappings()));
            setSpDialectClaims(claimConfig, spClaimMappings, tenantDomain);
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

        PermissionsAndRoleConfig permissionRoleConfiguration;
        permissionRoleConfiguration = application.getPermissionAndRoleConfig();

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

            RoleMapping[] tempRoleMappings = permissionRoleConfiguration.getRoleMappings();

            if (tempRoleMappings != null && tempRoleMappings.length > 0) {
                for (RoleMapping roleMapping : tempRoleMappings) {
                    this.roleMappings.put(roleMapping.getLocalRole().getLocalRoleName(),
                                          roleMapping.getRemoteRole());
                }
            }
        }

        ServiceProviderProperty[] spProperties = serviceProvider.getSpProperties();
        if (spProperties != null) {
            for (ServiceProviderProperty prop: spProperties) {
                if (IdentityApplicationConstants.USE_USER_ID_FOR_DEFAULT_SUBJECT.equals(prop.getName())) {
                    useUserIdForDefaultSubject = Boolean.parseBoolean(prop.getValue());
                    break;
                }
            }
        }
    }

    public void setUseUserstoreDomainInRole(boolean useUserstoreDomainInRole) {

        this.useUserstoreDomainInRole = useUserstoreDomainInRole;
    }

    public int getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(int applicationID) {
        this.applicationID = applicationID;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getRoleClaim() {
        return roleClaim;
    }

    public void setRoleClaim(String roleClaim) {
        this.roleClaim = roleClaim;
    }

    public String[] getPermissions() {
        if (permissions != null) {
            return permissions.clone();
        } else {
            return new String[0];
        }
    }

    public void setPermissions(String[] permissions) {
        if (permissions != null) {
            this.permissions = permissions.clone();
        }
    }

    public Map<String, String> getClaimMappings() {
        return claimMappings;
    }

    public void setClaimMappings(Map<String, String> claimMappings) {
        this.claimMappings = claimMappings;
    }

    public Map<String, String> getRequestedClaimMappings() {
        return requestedClaims;
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
        return mandatoryClaims;
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
        return roleMappings;
    }

    public void setRoleMappings(Map<String, String> roleMappings) {
        this.roleMappings = roleMappings;
    }

    public boolean noClaimMapping() {
        return claimMappings == null;
    }

    public boolean isAlwaysSendMappedLocalSubjectId() {
        return alwaysSendMappedLocalSubjectId;
    }

    public void setAlwaysSendMappedLocalSubjectId(boolean alwaysSendMappedLocalSubjectId) {
        this.alwaysSendMappedLocalSubjectId = alwaysSendMappedLocalSubjectId;
    }

    public boolean isMappedSubjectIDSelected() {
        return mappedSubjectIDSelected;
    }

    public void setMappedSubjectIDSelected(boolean mappedSubjectIDSelected) {
        this.mappedSubjectIDSelected = mappedSubjectIDSelected;
    }

    public String getSubjectClaimUri() {
        return subjectClaimUri;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public void setServiceProvider(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    public boolean isSaaSApp() {
        return isSaaSApp;
    }

    public boolean isUseTenantDomainInLocalSubjectIdentifier() {
        return useTenantDomainInLocalSubjectIdentifier;
    }

    public void setUseTenantDomainInLocalSubjectIdentifier(boolean useTenantDomainInLocalSubjectIdentifier) {
        this.useTenantDomainInLocalSubjectIdentifier = useTenantDomainInLocalSubjectIdentifier;
    }

    public boolean isUseUserstoreDomainInLocalSubjectIdentifier() {
        return useUserstoreDomainInLocalSubjectIdentifier;
    }

    public void setUseUserstoreDomainInLocalSubjectIdentifier(boolean useUserstoreDomainInLocalSubjectIdentifier) {
        this.useUserstoreDomainInLocalSubjectIdentifier = useUserstoreDomainInLocalSubjectIdentifier;
    }

    public boolean isEnableAuthorization() {

        return enableAuthorization;
    }

    public void setEnableAuthorization(boolean enableAuthorization) {

        this.enableAuthorization = enableAuthorization;
    }

    /**
     * This method will clone current class objects
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
        applicationConfig.setPermissions(this.permissions.clone());
        return applicationConfig;
    }

    /**
     * Set all the claim mappings of the configured SP claim dialects.
     *
     * @param claimConfig Application claim configuration
     * return Application claim mappings
     */
    private void setSpDialectClaims(ClaimConfig claimConfig, List<ClaimMapping> spClaimMappings, String tenantDomain) {

        String[] spClaimDialects = claimConfig.getSpClaimDialects();
        if (!ArrayUtils.isEmpty(spClaimDialects)) {
            List<String> spClaimDialectsList = Arrays.asList(spClaimDialects);
            spClaimDialectsList.forEach(spClaimDialect -> {
                try {
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

    public boolean isUseUserIdForDefaultSubject() {
        return useUserIdForDefaultSubject;
    }
}

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

import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ApplicationConfig implements Serializable {

    private static final long serialVersionUID = 8082478632322393384L;

    private ServiceProvider serviceProvider = null;

    private int applicationID = 0;
    private String applicationName = null;
    private String roleClaim = null;
    private boolean alwaysSendMappedLocalSubjectId = false;
    private boolean mappedSubjectIDSelected = false;
    private String subjectClaimUri;
    private String[] permissions = new String[0];
    private Map<String, String> claimMappings = new HashMap<String, String>();
    private Map<String, String> roleMappings = new HashMap<String, String>();
    private Map<String, String> requestedClaims = new HashMap<String, String>();
    private boolean isSaaSApp;
    private boolean useTenantDomainInLocalSubjectIdentifier = true;
    private boolean useUserstoreDomainInLocalSubjectIdentifier = true;

    public ApplicationConfig(ServiceProvider application) {
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
        }


        ClaimConfig claimConfig = application.getClaimConfig();
        if (claimConfig != null) {
            roleClaim = claimConfig.getRoleClaimURI();
            alwaysSendMappedLocalSubjectId = claimConfig.isAlwaysSendMappedLocalSubjectId();

            ClaimMapping[] claimMapping = claimConfig.getClaimMappings();

            requestedClaims = new HashMap<String, String>();

            if (claimMapping != null && claimMapping.length > 0) {
                claimMappings = new HashMap<String, String>();
                for (ClaimMapping claim : claimMapping) {
                    if (claim.getRemoteClaim() != null
                        && claim.getRemoteClaim().getClaimUri() != null) {
                        if (claim.getLocalClaim() != null) {
                            claimMappings.put(claim.getRemoteClaim().getClaimUri(), claim
                                    .getLocalClaim().getClaimUri());

                            if (claim.isRequested()) {
                                requestedClaims.put(claim.getRemoteClaim().getClaimUri(), claim
                                        .getLocalClaim().getClaimUri());
                            }

                        } else {
                            claimMappings.put(claim.getRemoteClaim().getClaimUri(), null);
                            if (claim.isRequested()) {
                                requestedClaims.put(claim.getRemoteClaim().getClaimUri(), null);
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
                this.roleMappings = new HashMap<String, String>();
                for (RoleMapping roleMapping : tempRoleMappings) {
                    this.roleMappings.put(roleMapping.getLocalRole().getLocalRoleName(),
                                          roleMapping.getRemoteRole());
                }
            }
        }
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
}

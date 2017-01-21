/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.gateway.common.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

public class ServiceProvider implements Serializable {

    private static final long serialVersionUID = 4754526832588478582L;
    private static final Log log = LogFactory.getLog(ServiceProvider.class);

    private int applicationID = 0;
    private String applicationName;
    private String description;
    private User owner;
    private InboundAuthenticationConfig inboundAuthenticationConfig;
    private LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig;
    private RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs;
    private InboundProvisioningConfig inboundProvisioningConfig;
    private OutboundProvisioningConfig outboundProvisioningConfig;
    private ClaimConfig claimConfig;
    private PermissionsAndRoleConfig permissionAndRoleConfig;
    private boolean saasApp;
    private ServiceProviderProperty []spProperties = new ServiceProviderProperty[0];


    /**
     * @return
     */
    public int getApplicationID() {
        return applicationID;
    }

    /**
     * @param applicationID
     */
    public void setApplicationID(int applicationID) {
        this.applicationID = applicationID;
    }

    /**
     * @return
     */
    public InboundAuthenticationConfig getInboundAuthenticationConfig() {
        return inboundAuthenticationConfig;
    }

    /**
     * @param inboundAuthenticationConfig
     */
    public void setInboundAuthenticationConfig(
            InboundAuthenticationConfig inboundAuthenticationConfig) {
        this.inboundAuthenticationConfig = inboundAuthenticationConfig;
    }

    /**
     * @return
     */
    public LocalAndOutboundAuthenticationConfig getLocalAndOutBoundAuthenticationConfig() {
        return localAndOutBoundAuthenticationConfig;
    }

    /**
     * @param localAndOutBoundAuthenticationConfig
     */
    public void setLocalAndOutBoundAuthenticationConfig(
            LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig) {
        this.localAndOutBoundAuthenticationConfig = localAndOutBoundAuthenticationConfig;
    }

    /**
     * @return
     */
    public RequestPathAuthenticatorConfig[] getRequestPathAuthenticatorConfigs() {
        return requestPathAuthenticatorConfigs;
    }

    /**
     * @param requestPathAuthenticatorConfigs
     */
    public void setRequestPathAuthenticatorConfigs(
            RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs) {
        this.requestPathAuthenticatorConfigs = requestPathAuthenticatorConfigs;
    }

    /**
     * @return
     */
    public InboundProvisioningConfig getInboundProvisioningConfig() {
        return inboundProvisioningConfig;
    }

    /**
     * @param inboundProvisioningConfig
     */
    public void setInboundProvisioningConfig(InboundProvisioningConfig inboundProvisioningConfig) {
        this.inboundProvisioningConfig = inboundProvisioningConfig;
    }

    /**
     * @return
     */
    public OutboundProvisioningConfig getOutboundProvisioningConfig() {
        return outboundProvisioningConfig;
    }

    /**
     * @param outboundProvisioningConfig
     */
    public void setOutboundProvisioningConfig(OutboundProvisioningConfig outboundProvisioningConfig) {
        this.outboundProvisioningConfig = outboundProvisioningConfig;
    }

    /**
     * @return
     */
    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    /**
     * @param claimConfig
     */
    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    /**
     * @return
     */
    public PermissionsAndRoleConfig getPermissionAndRoleConfig() {
        return permissionAndRoleConfig;
    }

    /**
     * @param permissionAndRoleConfig
     */
    public void setPermissionAndRoleConfig(PermissionsAndRoleConfig permissionAndRoleConfig) {
        this.permissionAndRoleConfig = permissionAndRoleConfig;
    }

    /**
     * @return
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @param applicationName
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * @return
     */
    public User getOwner() {
        return owner;
    }

    /**
     * @param owner
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSaasApp() {
        return saasApp;
    }

    public void setSaasApp(boolean saasApp) {
        this.saasApp = saasApp;
    }

    public ServiceProviderProperty[] getSpProperties() {
        return spProperties;
    }

    public void setSpProperties(ServiceProviderProperty[] spProperties) {
        this.spProperties = spProperties;
    }
}
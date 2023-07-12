/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.common.model;

/**
 * This class represents the basic service provider information.
 * The purpose of this class is to store load service provider configuration with minimal load.
 */
public class LiteServiceProvider {

    private static final long serialVersionUID = 6754526832588478582L;
    private int applicationID = 0;

    private String applicationName;

    private String description;

    private String certificateContent;

    private String jwksUri;

    private User owner;

    private InboundAuthenticationConfig inboundAuthenticationConfig;

    private LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig;

    private RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs;

    private InboundProvisioningConfig inboundProvisioningConfig;

    private OutboundProvisioningConfig outboundProvisioningConfig;

    private ClaimConfig claimConfig;

    private PermissionsAndRoleConfig permissionAndRoleConfig;

    private boolean saasApp;

    private ServiceProviderProperty[] spProperties = new ServiceProviderProperty[0];

    private String applicationResourceId;

    private String imageUrl;

    private String accessUrl;

    private boolean isDiscoverable;


    public int getApplicationID() {
        return applicationID;
    }

    public void setApplicationID(int applicationID) {
        this.applicationID = applicationID;
    }

    public LocalAndOutboundAuthenticationConfig getLocalAndOutBoundAuthenticationConfig() {
        return localAndOutBoundAuthenticationConfig;
    }

    public void setLocalAndOutBoundAuthenticationConfig(
            LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig) {
        this.localAndOutBoundAuthenticationConfig = localAndOutBoundAuthenticationConfig;
    }

    public ClaimConfig getClaimConfig() {
        return claimConfig;
    }

    public void setClaimConfig(ClaimConfig claimConfig) {
        this.claimConfig = claimConfig;
    }

    public PermissionsAndRoleConfig getPermissionAndRoleConfig() {
        return permissionAndRoleConfig;
    }

    public void setPermissionAndRoleConfig(PermissionsAndRoleConfig permissionAndRoleConfig) {
        this.permissionAndRoleConfig = permissionAndRoleConfig;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public User getOwner() {
        return owner;
    }

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

    /**
     *
     * Returns the certificate content
     *
     * @return the certificate in PEM format.
     */
    public String getCertificateContent() {
        return certificateContent;
    }

    /**
     *
     * Sets the given certificate.
     *
     * @param certificateContent the certificate in PEM format
     */
    public void setCertificateContent(String certificateContent) {
        this.certificateContent = certificateContent;
    }

    public String getApplicationResourceId() {

        return applicationResourceId;
    }

    public void setApplicationResourceId(String applicationResourceId) {

        this.applicationResourceId = applicationResourceId;
    }

    public String getImageUrl() {

        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    public String getAccessUrl() {

        return accessUrl;
    }

    public void setAccessUrl(String accessUrl) {

        this.accessUrl = accessUrl;
    }

    public String getJwksUri() {

        return jwksUri;
    }

    public void setJwksUri(String jwksUri) {

        this.jwksUri = jwksUri;
    }

    public boolean isDiscoverable() {

        return isDiscoverable;
    }

    public void setDiscoverable(boolean discoverable) {

        isDiscoverable = discoverable;
    }
}

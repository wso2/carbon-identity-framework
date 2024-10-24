/*
 * Copyright (c) 2014-2023, WSO2 LLC. (http://www.wso2.com).
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.databinding.annotation.IgnoreNullElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Application configuration.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ServiceProvider")
public class ServiceProvider implements Serializable {

    private static final long serialVersionUID = 4754526832588478582L;
    private static final Log log = LogFactory.getLog(ServiceProvider.class);
    private static final String CONSENT_CONFIG_ELEM = "ConsentConfig";

    private static final String APPLICATION_VERSION = "ApplicationVersion";
    private static final String ACCESS_URL = "AccessUrl";
    private static final String IMAGE_URL = "ImageUrl";
    private static final String TEMPLATE_ID = "TemplateId";
    private static final String TEMPLATE_VERSION = "TemplateVersion";
    private static final String IS_MANAGEMENT_APP = "IsManagementApp";

    private static final String IS_B2B_SELF_SERVICE_APP = "IsB2BSelfServiceApp";
    private static final String IS_APPLICATION_ENABLED = "IsApplicationEnabled";
    private static final String ASSOCIATED_ROLES_CONFIG = "AssociatedRolesConfig";
    private static final String IS_API_BASED_AUTHENTICATION_ENABLED = "IsAPIBasedAuthenticationEnabled";
    private static final String TRUSTED_APP_METADATA = "TrustedAppMetadata";

    @XmlTransient
    @JsonIgnore
    private int applicationID = 0;

    @XmlElement(name = "ApplicationName")
    private String applicationName;

    @XmlElement(name = APPLICATION_VERSION)
    private String applicationVersion;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "Certificate")
    private String certificateContent;

    @XmlElement(name = "JwksUri")
    private String jwksUri;

    @XmlTransient
    @JsonIgnore
    private User owner;

    @XmlTransient
    @JsonIgnore
    private String tenantDomain;

    @XmlElement(name = "InboundAuthenticationConfig")
    private InboundAuthenticationConfig inboundAuthenticationConfig;

    @XmlElement(name = "LocalAndOutBoundAuthenticationConfig")
    private LocalAndOutboundAuthenticationConfig localAndOutBoundAuthenticationConfig;

    @XmlElementWrapper(name = "RequestPathAuthenticatorConfigs")
    @XmlElement(name = "RequestPathAuthenticatorConfig")
    private RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigs;

    @XmlElement(name = "InboundProvisioningConfig")
    private InboundProvisioningConfig inboundProvisioningConfig;

    @XmlElement(name = "OutboundProvisioningConfig")
    private OutboundProvisioningConfig outboundProvisioningConfig;

    @XmlElement(name = "ClaimConfig")
    private ClaimConfig claimConfig;

    @XmlElement(name = "PermissionAndRoleConfig")
    private PermissionsAndRoleConfig permissionAndRoleConfig;

    @XmlElement(name = "IsSaaSApp")
    private boolean saasApp;

    @XmlTransient
    @JsonIgnore
    private ServiceProviderProperty[] spProperties = new ServiceProviderProperty[0];

    @IgnoreNullElement
    @XmlTransient
    @JsonIgnore
    private String applicationResourceId;

    @IgnoreNullElement
    @XmlElement(name = IMAGE_URL)
    private String imageUrl;

    @IgnoreNullElement
    @XmlElement(name = ACCESS_URL)
    private String accessUrl;

    @XmlElement(name = "IsDiscoverable")
    private boolean isDiscoverable;

    @IgnoreNullElement
    @XmlElement(name = TEMPLATE_ID)
    private String templateId;

    @IgnoreNullElement
    @XmlElement(name = TEMPLATE_VERSION)
    private String templateVersion;

    @IgnoreNullElement
    @XmlElement(name = IS_MANAGEMENT_APP)
    private boolean isManagementApp;

    @IgnoreNullElement
    @XmlElement(name = IS_B2B_SELF_SERVICE_APP)
    private boolean isB2BSelfServiceApp;

    @XmlElement(name = ASSOCIATED_ROLES_CONFIG)
    private AssociatedRolesConfig associatedRolesConfig;

    @IgnoreNullElement
    @XmlElement(name = IS_APPLICATION_ENABLED)
    private boolean isApplicationEnabled = true;

    @IgnoreNullElement
    @XmlElement(name = IS_API_BASED_AUTHENTICATION_ENABLED)
    private boolean isAPIBasedAuthenticationEnabled;

    @IgnoreNullElement
    @XmlElement(name = "ClientAttestationMetaData")
    private ClientAttestationMetaData clientAttestationMetaData;

    @IgnoreNullElement
    @XmlElement(name = TRUSTED_APP_METADATA)
    private SpTrustedAppMetadata trustedAppMetadata;

    /*
     * <ServiceProvider> <ApplicationID></ApplicationID> <Description></Description>
     * <Owner>....</Owner>
     * <IsSaaSApp>....</IsSaaSApp><InboundAuthenticationConfig>..</InboundAuthenticationConfig>
     * <LocalAndOutBoundAuthenticationConfig>..</LocalAndOutBoundAuthenticationConfig>
     * <RequestPathAuthenticatorConfigs>...</RequestPathAuthenticatorConfigs>
     * <InboundProvisioningConfig>...</InboundProvisioningConfig>
     * <OutboundProvisioningConfig>..</OutboundProvisioningConfig>
     * <PermissionAndRoleConfig>...</PermissionAndRoleConfig> <ClaimConfig>...</ClaimConfig>
     * </ServiceProvider>
     */
    public static ServiceProvider build(OMElement serviceProviderOM) {

        ServiceProvider serviceProvider = new ServiceProvider();

        // by default set to true.
        serviceProvider.setSaasApp(true);
        serviceProvider.setApplicationEnabled(true);

        Iterator<?> iter = serviceProviderOM.getChildElements();

        while (iter.hasNext()) {

            OMElement element = (OMElement) (iter.next());
            String elementName = element.getLocalName();

            if ("ApplicationID".equals(elementName)) {
                if (element.getText() != null) {
                    serviceProvider.setApplicationID(Integer.parseInt(element.getText()));
                }
            } else if ("ApplicationName".equals(elementName)) {
                if (element.getText() != null) {
                    serviceProvider.setApplicationName(element.getText());
                    serviceProvider.setApplicationResourceId(serviceProvider.getApplicationName());
                } else {
                    log.error("Service provider not loaded from the file. Application Name is null.");
                    return null;
                }
            } else if (APPLICATION_VERSION.equals(elementName)) {
                serviceProvider.setApplicationVersion(element.getText());
            } else if ("Description".equals(elementName)) {
                serviceProvider.setDescription(element.getText());
            } else if (IMAGE_URL.equals(elementName)) {
                serviceProvider.setImageUrl(element.getText());
            } else if (ACCESS_URL.equals(elementName)) {
                serviceProvider.setAccessUrl(element.getText());
            } else if (TEMPLATE_ID.equals(elementName)) {
                serviceProvider.setTemplateId(element.getText());
            } else if (TEMPLATE_VERSION.equals(elementName)) {
                serviceProvider.setTemplateVersion(element.getText());
            } else if ("Certificate".equals(elementName)) {
                serviceProvider.setCertificateContent(element.getText());
            } else if ("JwksUri".equals(elementName)) {
                serviceProvider.setJwksUri(element.getText());
            } else if (IS_API_BASED_AUTHENTICATION_ENABLED.equals(elementName)) {
                boolean isAPIBasedAuthEnabled = element.getText() != null && "true".equals(element.getText());
                serviceProvider.setAPIBasedAuthenticationEnabled(isAPIBasedAuthEnabled);
            } else if ("ClientAttestationMetaData".equals(elementName)) {
                // build client attestation meta data configuration.
                serviceProvider
                        .setClientAttestationMetaData(ClientAttestationMetaData
                                .build(element));
            } else if ("IsSaaSApp".equals(elementName)) {
                if (element.getText() != null && "true".equals(element.getText())) {
                    serviceProvider.setSaasApp(true);
                } else {
                    serviceProvider.setSaasApp(false);
                }
            } else if ("Owner".equals(elementName)) {
                // build service provider owner.
                serviceProvider.setOwner(User.build(element));
            } else if ("InboundAuthenticationConfig".equals(elementName)) {
                // build in-bound authentication configuration.
                serviceProvider.setInboundAuthenticationConfig(InboundAuthenticationConfig
                        .build(element));
            } else if ("LocalAndOutBoundAuthenticationConfig".equals(elementName)) {
                // build local and out-bound authentication configuration.
                serviceProvider
                        .setLocalAndOutBoundAuthenticationConfig(LocalAndOutboundAuthenticationConfig
                                .build(element));
            } else if ("RequestPathAuthenticatorConfigs".equals(elementName)) {
                // build request-path authentication configurations.
                Iterator<?> requestPathAuthenticatorConfigsIter = element.getChildElements();

                if (requestPathAuthenticatorConfigsIter == null) {
                    continue;
                }

                List<RequestPathAuthenticatorConfig> requestPathAuthenticatorConfigsArrList;
                requestPathAuthenticatorConfigsArrList = new ArrayList<RequestPathAuthenticatorConfig>();

                while (requestPathAuthenticatorConfigsIter.hasNext()) {
                    OMElement requestPathAuthenticatorConfigsElement = (OMElement) (requestPathAuthenticatorConfigsIter
                            .next());
                    RequestPathAuthenticatorConfig reqConfig = RequestPathAuthenticatorConfig
                            .build(requestPathAuthenticatorConfigsElement);

                    if (reqConfig != null) {
                        // we only need not-null values.
                        requestPathAuthenticatorConfigsArrList.add(reqConfig);
                    }
                }

                if (CollectionUtils.isNotEmpty(requestPathAuthenticatorConfigsArrList)) {
                    // add to the service provider, only if we have any.
                    RequestPathAuthenticatorConfig[] requestPathAuthenticatorConfigsArr;
                    requestPathAuthenticatorConfigsArr = requestPathAuthenticatorConfigsArrList
                            .toArray(new RequestPathAuthenticatorConfig[0]);
                    serviceProvider
                            .setRequestPathAuthenticatorConfigs(requestPathAuthenticatorConfigsArr);
                }

            } else if ("InboundProvisioningConfig".equals(elementName)) {
                // build in-bound provisioning configuration.
                serviceProvider.setInboundProvisioningConfig(InboundProvisioningConfig
                        .build(element));
            } else if ("OutboundProvisioningConfig".equals(elementName)) {
                // build out-bound provisioning configuration.
                serviceProvider.setOutboundProvisioningConfig(OutboundProvisioningConfig
                        .build(element));
            } else if ("ClaimConfig".equals(elementName)) {
                // build claim configuration.
                serviceProvider.setClaimConfig(ClaimConfig.build(element));
            } else if ("PermissionAndRoleConfig".equals(elementName)) {
                // build permission and role configuration.
                serviceProvider.setPermissionAndRoleConfig(PermissionsAndRoleConfig.build(element));
            } else if (TRUSTED_APP_METADATA.equals(elementName)) {
                // build trusted app metadata.
                serviceProvider.setTrustedAppMetadata(SpTrustedAppMetadata.build(element));
            } else if (ASSOCIATED_ROLES_CONFIG.equals(elementName)) {
                // build role association.
                serviceProvider.setAssociatedRolesConfig(AssociatedRolesConfig.build(element));
            } else if (IS_APPLICATION_ENABLED.equals(elementName)) {
                if (element.getText() != null && "true".equals(element.getText())) {
                    serviceProvider.setApplicationEnabled(true);
                } else  {
                    serviceProvider.setApplicationEnabled(!"false".equals(element.getText()));
                }
            }
        }

        return serviceProvider;
    }

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
     * Get associated roles config.
     *
     * @return AssociatedRolesConfig.
     */
    public AssociatedRolesConfig getAssociatedRolesConfig() {

        return associatedRolesConfig;
    }

    /**
     * Set associated roles config.
     *
     * @param associatedRolesConfig AssociatedRolesConfig.
     */
    public void setAssociatedRolesConfig(AssociatedRolesConfig associatedRolesConfig) {

        this.associatedRolesConfig = associatedRolesConfig;
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
     * @return Application version.
     */
    public String getApplicationVersion() {

        return applicationVersion;
    }

    /**
     * @param applicationVersion Application version.
     */
    public void setApplicationVersion(String applicationVersion) {

        this.applicationVersion = applicationVersion;
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

    /**
     * Gets the Service Provider tenant domain.
     *
     * @return Service Provider tenant domain
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Sets the Service Provider tenant domain.
     *
     * @param tenantDomain  Service Provider tenant domain
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
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

    public String getTemplateId() {

        return templateId;
    }

    public void setTemplateId(String templateId) {

        this.templateId = templateId;
    }

    /**
     * Retrieve the template version of the current service provider.
     *
     * @return Template version.
     */
    public String getTemplateVersion() {

        return templateVersion;
    }

    /**
     * Set a new template version for the current service provider.
     *
     * @param templateVersion Template version to be set.
     */
    public void setTemplateVersion(String templateVersion) {

        this.templateVersion = templateVersion;
    }

    public boolean isManagementApp() {

        return isManagementApp;
    }

    public void setManagementApp(boolean managementApp) {

        isManagementApp = managementApp;
    }

    public boolean isB2BSelfServiceApp() {

        return isB2BSelfServiceApp;
    }

    public void setB2BSelfServiceApp(boolean isB2BSelfServiceApp) {

        this.isB2BSelfServiceApp = isB2BSelfServiceApp;
    }

    public boolean isAPIBasedAuthenticationEnabled() {

        return isAPIBasedAuthenticationEnabled;
    }

    public void setAPIBasedAuthenticationEnabled(boolean isAPIBasedAuthenticationEnabled) {

        this.isAPIBasedAuthenticationEnabled = isAPIBasedAuthenticationEnabled;
    }
    public ClientAttestationMetaData getClientAttestationMetaData() {

        return clientAttestationMetaData;
    }

    public void setClientAttestationMetaData(ClientAttestationMetaData clientAttestationMetaData) {

        this.clientAttestationMetaData = clientAttestationMetaData;
    }

    public SpTrustedAppMetadata getTrustedAppMetadata() {

        return trustedAppMetadata;
    }

    public void setTrustedAppMetadata(SpTrustedAppMetadata trustedAppMetadata) {

        this.trustedAppMetadata = trustedAppMetadata;
    }

    public boolean isApplicationEnabled() {

        return isApplicationEnabled;
    }

    public void setApplicationEnabled(boolean applicationEnabled) {

        this.isApplicationEnabled = applicationEnabled;
    }
}


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
package org.wso2.carbon.identity.application.common.model;

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

    private static final String ACCESS_URL = "AccessUrl";
    private static final String IMAGE_URL = "ImageUrl";

    @XmlTransient
    private int applicationID = 0;

    @XmlElement(name = "ApplicationName")
    private String applicationName;

    @XmlElement(name = "Description")
    private String description;

    @XmlElement(name = "Certificate")
    private String certificateContent;

    @XmlElement(name = "JwksUri")
    private String jwksUri;

    @XmlTransient
    private User owner;

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
    private ServiceProviderProperty[] spProperties = new ServiceProviderProperty[0];

    @IgnoreNullElement
    @XmlTransient
    private String applicationResourceId;

    @IgnoreNullElement
    @XmlElement(name = IMAGE_URL)
    private String imageUrl;

    @IgnoreNullElement
    @XmlElement(name = ACCESS_URL)
    private String accessUrl;

    @XmlElement(name = "IsDiscoverable")
    private boolean isDiscoverable;


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
                } else {
                    log.error("Service provider not loaded from the file. Application Name is null.");
                    return null;
                }
            } else if ("Description".equals(elementName)) {
                serviceProvider.setDescription(element.getText());
            } else if (IMAGE_URL.equals(elementName)) {
                serviceProvider.setImageUrl(element.getText());
            } else if (ACCESS_URL.equals(elementName)) {
                serviceProvider.setAccessUrl(element.getText());
            } else if ("Certificate".equals(elementName)) {
                serviceProvider.setCertificateContent(element.getText());
            } else if ("JwksUri".equals(elementName)) {
                serviceProvider.setJwksUri(element.getText());
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


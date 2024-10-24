/*
 * Copyright (c) 2014, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;

/**
 * Basic information of an application.
 */
public class ApplicationBasicInfo implements Serializable {

    private static final long serialVersionUID = -16127229981193883L;

    private int applicationId;
    private String applicationName;
    private String applicationVersion;
    private String description;

    private String applicationResourceId;
    private String imageUrl;
    private String accessUrl;

    private User appOwner;
    private String clientId;
    private String issuer;
    private String tenantDomain;
    private String uuid;

    /**
     * Get application id.
     *
     * @return Application id.
     */
    public int getApplicationId() {

        return applicationId;
    }

    /**
     * Set application id.
     *
     * @param applicationId Application id.
     */
    public void setApplicationId(int applicationId) {

        this.applicationId = applicationId;
    }

    /**
     * Get application name.
     *
     * @return Application name.
     */
    public String getApplicationName() {

        return applicationName;
    }

    /**
     * Set application name.
     *
     * @param applicationName   Application name.
     */
    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    /**
     * Get application version.
     *
     * @return Application version.
     */
    public String getApplicationVersion() {

        return applicationVersion;
    }

    /**
     * Set application version.
     *
     * @param applicationVersion Application version.
     */
    public void setApplicationVersion(String applicationVersion) {

        this.applicationVersion = applicationVersion;
    }

    /**
     * Get application description.
     *
     * @return Description.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set application description.
     *
     * @param description   Description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get application resource id.
     *
     * @return Application resource id.
     */
    public String getApplicationResourceId() {

        return applicationResourceId;
    }

    /**
     * Set application resource id.
     *
     * @param applicationResourceId Application resource id.
     */
    public void setApplicationResourceId(String applicationResourceId) {

        this.applicationResourceId = applicationResourceId;
    }

    /**
     * Get image URL.
     *
     * @return Image URL.
     */
    public String getImageUrl() {

        return imageUrl;
    }

    /**
     * Set application image URL.
     *
     * @param imageUrl  Application image URL.
     */
    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    /**
     * Get access URL.
     *
     * @return Access URL.
     */
    public String getAccessUrl() {

        return accessUrl;
    }

    /**
     * Set access URL.
     *
     * @param accessUrl Access URL.
     */
    public void setAccessUrl(String accessUrl) {

        this.accessUrl = accessUrl;
    }

    /**
     * Get app owner details.
     *
     * @return App owner details.
     */
    public User getAppOwner() {

        return appOwner;
    }

    /**
     * Set application owner details.
     *
     * @param appOwner  Application owner details.
     */
    public void setAppOwner(User appOwner) {

        this.appOwner = appOwner;
    }

    /**
     * Get OAuth2 client ID.
     *
     * @return OAuth2 client ID.
     */
    public String getClientId() {

        return clientId;
    }

    /**
     * Set OAuth2 client ID.
     *
     * @param clientId  OAuth2 client ID.
     */
    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    /**
     * Get SAML issuer.
     *
     * @return SAML issuer.
     */
    public String getIssuer() {

        return issuer;
    }

    /**
     * Set SAML issuer.
     *
     * @param issuer  SAML issuer.
     */
    public void setIssuer(String issuer) {

        this.issuer = issuer;
    }

    /**
     * Get application tenant domain.
     *
     * @return Tenant domain
     */
    public String getTenantDomain() {

        return tenantDomain;
    }

    /**
     * Set application tenant domain.
     *
     * @param tenantDomain Application tenant domain
     */
    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    /**
     * Get application uuid.
     *
     * @return uuid application uuid
     */
    public String getUuid() {

        return uuid;
    }

    /**
     * Set application uuid.
     *
     * @param uuid application uuid
     */
    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

}

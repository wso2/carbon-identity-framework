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
    private String description;

    private String applicationResourceId;
    private String imageUrl;
    private String accessUrl;

    private User appOwner;
    private String inboundKey;

    /**
     * Get application id.
     *
     * @return application id.
     */
    public int getApplicationId() {

        return applicationId;
    }

    /**
     * Set application id.
     *
     * @param applicationId application id.
     */
    public void setApplicationId(int applicationId) {

        this.applicationId = applicationId;
    }

    /**
     * Get application name.
     *
     * @return application name.
     */
    public String getApplicationName() {

        return applicationName;
    }

    /**
     * Set application name.
     *
     * @param applicationName   application name.
     */
    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    /**
     * Get application description.
     *
     * @return description.
     */
    public String getDescription() {

        return description;
    }

    /**
     * Set application description.
     *
     * @param description   description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get application resource id.
     *
     * @return application resource id.
     */
    public String getApplicationResourceId() {

        return applicationResourceId;
    }

    /**
     * Set application resource id.
     *
     * @param applicationResourceId application resource id.
     */
    public void setApplicationResourceId(String applicationResourceId) {

        this.applicationResourceId = applicationResourceId;
    }

    /**
     * Get image URL.
     *
     * @return image URL.
     */
    public String getImageUrl() {

        return imageUrl;
    }

    /**
     * Set application image URL.
     *
     * @param imageUrl  application image URL.
     */
    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    /**
     * Get access URL.
     *
     * @return access URL.
     */
    public String getAccessUrl() {

        return accessUrl;
    }

    /**
     * Set access URL.
     *
     * @param accessUrl access URL.
     */
    public void setAccessUrl(String accessUrl) {

        this.accessUrl = accessUrl;
    }

    /**
     * Get app owner details.
     *
     * @return app owner details.
     */
    public User getAppOwner() {

        return appOwner;
    }

    /**
     * Set application owner details.
     *
     * @param appOwner  application owner details.
     */
    public void setAppOwner(User appOwner) {

        this.appOwner = appOwner;
    }

    /**
     * Get inbound auth key.
     *
     * @return inbound auth key.
     */
    public String getInboundKey() {

        return inboundKey;
    }

    /**
     * Set inbound auth key.
     *
     * @param inboundKey    inbound auth key.
     */
    public  void setInboundKey(String inboundKey) {

        this.inboundKey = inboundKey;
    }
}

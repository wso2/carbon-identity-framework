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

    /**
     * Get application id.
     *
     * @return application id
     */
    public int getApplicationId() {

        return applicationId;
    }

    /**
     * Set application id.
     *
     * @param applicationId application id
     */
    public void setApplicationId(int applicationId) {

        this.applicationId = applicationId;
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
    public String getDescription() {

        return description;
    }

    /**
     * @param description
     */
    public void setDescription(String description) {

        this.description = description;
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

    public User getAppOwner() {

        return appOwner;
    }

    public void setAppOwner(User appOwner) {

        this.appOwner = appOwner;
    }
}

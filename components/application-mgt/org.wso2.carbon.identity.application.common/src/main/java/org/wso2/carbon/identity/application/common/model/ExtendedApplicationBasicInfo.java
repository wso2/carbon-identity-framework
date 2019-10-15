/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.identity.application.common.model;

/**
 * Holds application basic information.
 * <p>
 * This is the first step to decouple the model object used by the OSGi layer with the one used in the SOAP service.
 */
public class ExtendedApplicationBasicInfo extends ApplicationBasicInfo {

    private int applicationId;
    private String applicationName;
    private String description;
    private String applicationResourceId;
    private String imageUrl;
    private String loginUrl;

    public int getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(int applicationId) {

        this.applicationId = applicationId;
    }

    public String getApplicationName() {

        return applicationName;
    }

    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    public String getDescription() {

        return description;
    }

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

    public String getLoginUrl() {

        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {

        this.loginUrl = loginUrl;
    }
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.model;

/**
 * This class represents an application.
 */
public class Application {

    private String resourceId;
    private String appId;
    private String subject;
    private String appName;

    public Application(String subject, String appName) {
        this.appName = appName;
        this.subject = subject;
    }

    public Application(String subject, String appName, String appId) {

        this.appId = appId;
        this.subject = subject;
        this.appName = appName;
    }

    public Application(String subject, String appName, String appId, String resourceId) {

        this.appId = appId;
        this.subject = subject;
        this.appName = appName;
        this.resourceId = resourceId;
    }

    public String getSubject() {
        return subject;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppId() {
        return appId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getResourceId() {

        return resourceId;
    }
}

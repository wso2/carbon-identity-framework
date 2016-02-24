/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.core.model;

import java.util.Date;

public class OpenIDUserRPDO {

    private String uuid;
    private String rpUrl;
    private String userName;
    private boolean isTrustedAlways;
    private int visitCount;
    private Date lastVisit;
    private String defaultProfileName;

    public String getRpUrl() {
        return this.rpUrl;
    }

    public void setRpUrl(String rpUrl) {
        this.rpUrl = rpUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isTrustedAlways() {
        return isTrustedAlways;
    }

    public void setTrustedAlways(boolean isTrustedAlways) {
        this.isTrustedAlways = isTrustedAlways;
    }

    public int getVisitCount() {
        return this.visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public Date getLastVisit() {
        return this.lastVisit;
    }

    public void setLastVisit(Date lastVisit) {
        this.lastVisit = lastVisit;
    }

    public String getDefaultProfileName() {
        return defaultProfileName;
    }

    public void setDefaultProfileName(String defaultProfileName) {
        this.defaultProfileName = defaultProfileName;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

}

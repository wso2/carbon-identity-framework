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

import java.util.List;

/**
 * UserSession is the class that represents a session object of a user.
 * This state of information includes:
 * <ul>
 * <li>User ID
 * <li>Session ID
 * <li>User agent
 * <li>IP address
 * <li>Creation time
 * <li>Login time
 * <li>Last access time
 * <li>Applications that belongs to that session
 * </ul>
 */
public class UserSession {

    public UserSession() {

    }

    private String userId;
    private String sessionId;
    private String userAgent;
    private String ip;
    private String loginTime;
    private String lastAccessTime;
    private Long creationTime;
    private List<Application> applications;

    public String getUserAgent() {

        return userAgent;
    }

    public void setUserAgent(String userAgent) {

        this.userAgent = userAgent;
    }

    public String getIp() {

        return ip;
    }

    public void setIp(String ip) {

        this.ip = ip;
    }

    public String getLoginTime() {

        return loginTime;
    }

    public void setLoginTime(String loginTime) {

        this.loginTime = loginTime;
    }

    public String getLastAccessTime() {

        return lastAccessTime;
    }

    public void setLastAccessTime(String lastAccessTime) {

        this.lastAccessTime = lastAccessTime;
    }

    public List<Application> getApplications() {

        return applications;
    }

    public void setApplications(List<Application> applications) {

        this.applications = applications;
    }

    public String getSessionId() {

        return sessionId;
    }

    public void setSessionId(String sessionId) {

        this.sessionId = sessionId;
    }

    public String getUserId() {

        return userId;
    }

    public void setUserId(String userId) {

        this.userId = userId;
    }

    public Long getCreationTime() {

        return creationTime;
    }

    public void setCreationTime(Long creationTime) {

        this.creationTime = creationTime;
    }
}

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

package org.wso2.carbon.identity.application.authentication.framework.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AuthenticationResult implements Serializable {

    private static final long serialVersionUID = 462700737456383154L;

    private boolean authenticated;
    private AuthenticatedUser subject;
    private String authenticatedIdPs;
    private String authenticatedAuthenticators;
    private boolean isSaaSApp;
    private boolean loggedOut;
    private Map<String, String> claimMapping;
    private Map<String, Object> properties = new HashMap<>();

    public AuthenticationResult() {
    }

    public AuthenticationResult(boolean authenticated, AuthenticatedUser subject, String authenticatedAuthenticators) {

        this.authenticated = authenticated;
        this.subject = subject;
        this.authenticatedAuthenticators = authenticatedAuthenticators;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }

    public AuthenticatedUser getSubject() {
        return subject;
    }

    public void setSubject(AuthenticatedUser subject) {
        this.subject = subject;
    }

    public String getAuthenticatedAuthenticators() {
        return authenticatedAuthenticators;
    }

    public void setAuthenticatedAuthenticators(String authenticatedAuthenticators) {
        this.authenticatedAuthenticators = authenticatedAuthenticators;
    }

    public boolean isLoggedOut() {
        return loggedOut;
    }

    public void setLoggedOut(boolean loggedOut) {
        this.loggedOut = loggedOut;
    }

    public Map<String, String> getClaimMapping() {
        return claimMapping;
    }

    public void setClaimMapping(Map<String, String> claimMapping) {
        this.claimMapping = claimMapping;
    }

    public String getAuthenticatedIdPs() {
        return authenticatedIdPs;
    }

    public void setAuthenticatedIdPs(String authenticatedIdPs) {
        this.authenticatedIdPs = authenticatedIdPs;
    }

    public boolean isSaaSApp() {
        return isSaaSApp;
    }

    public void setSaaSApp(boolean isSaaSApp) {
        this.isSaaSApp = isSaaSApp;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
}

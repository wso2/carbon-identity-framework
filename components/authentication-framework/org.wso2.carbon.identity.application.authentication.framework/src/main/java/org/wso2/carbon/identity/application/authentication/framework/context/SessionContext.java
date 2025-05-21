/*
 * Copyright (c) 2013-2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.context;

import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Session context bean.
 */
public class SessionContext implements Serializable {

    private static final long serialVersionUID = -2381634092699961018L;

    private Map<String, SequenceConfig> authenticatedSequences = new HashMap<>();
    private Map<String, AuthenticatedIdPData> authenticatedIdPs = new HashMap<>();
    private boolean isRememberMe = false;
    private Map<String, Object> properties = new HashMap<>();
    private SessionAuthHistory sessionAuthHistory = new SessionAuthHistory();
    // This authenticatedIdPsOfApp has the mapping of application and the map of authenticated IDPs.
    private Map<String, Map<String, AuthenticatedIdPData>> authenticatedIdPsOfApp;
    private String impersonatedUser;

    public Map<String, SequenceConfig> getAuthenticatedSequences() {
        return authenticatedSequences;
    }

    public void setAuthenticatedSequences(
            Map<String, SequenceConfig> authenticatedSequences) {
        this.authenticatedSequences = authenticatedSequences;
    }

    public Map<String, AuthenticatedIdPData> getAuthenticatedIdPs() {

        return authenticatedIdPs;
    }

    public void setAuthenticatedIdPs(Map<String, AuthenticatedIdPData> authenticatedIdPs) {
        this.authenticatedIdPs = authenticatedIdPs;
    }

    public Map<String, AuthenticatedIdPData> getAuthenticatedIdPsOfApp(String app) {

        if (authenticatedIdPsOfApp != null) {
            return authenticatedIdPsOfApp.get(app);
        }
        return new HashMap<>();
    }

    public void setAuthenticatedIdPsOfApp(String app, Map<String, AuthenticatedIdPData> authenticatedIdPsOfApp) {

        if (this.authenticatedIdPsOfApp == null) {
            this.authenticatedIdPsOfApp = new HashMap<>();
        }
        this.authenticatedIdPsOfApp.put(app, authenticatedIdPsOfApp);
    }

    public String getImpersonatedUser() {

        return impersonatedUser;
    }

    public void setImpersonatedUser(String impersonatedUser) {

        this.impersonatedUser = impersonatedUser;
    }

    public boolean isRememberMe() {
        return isRememberMe;
    }

    public void setRememberMe(boolean isRememberMe) {
        this.isRememberMe = isRememberMe;
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public SessionAuthHistory getSessionAuthHistory() {
        return sessionAuthHistory;
    }

    public void setSessionAuthHistory(SessionAuthHistory sessionAuthHistory) {

        this.sessionAuthHistory = sessionAuthHistory;
    }

    public Map<String, Object> getProperties() {

        return properties;
    }

    public void setProperties(Map<String, Object> properties) {

        this.properties = properties;
    }

    public Map<String, Map<String, AuthenticatedIdPData>> getAuthenticatedIdPsOfApp() {

        return authenticatedIdPsOfApp;
    }

    public void setAuthenticatedIdPsOfApp(
            Map<String, Map<String, AuthenticatedIdPData>> authenticatedIdPsOfApp) {

        this.authenticatedIdPsOfApp = authenticatedIdPsOfApp;
    }
}

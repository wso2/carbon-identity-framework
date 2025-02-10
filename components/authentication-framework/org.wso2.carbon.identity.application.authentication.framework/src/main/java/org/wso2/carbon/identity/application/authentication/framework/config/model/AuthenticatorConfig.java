/*
 * Copyright (c) 2013-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a wrapper class for ApplicationAuthenticator.
 */
public class AuthenticatorConfig implements Serializable {

    private static final long serialVersionUID = 4391415512399764048L;

    private String name;
    private boolean enabled;
    private ApplicationAuthenticator applicationAuthenticator;
    private AuthenticatorStateInfo authenticatorStateInfo;
    private Map<String, String> parameterMap;
    private Map<String, IdentityProvider> idps = new HashMap<>();
    private List<String> idpNames = new ArrayList<>();
    private List<String> idPResourceIds = new ArrayList<>();
    private String tenantDomain;

    public AuthenticatorConfig() {
    }

    public AuthenticatorConfig(String name, boolean enabled,
                               Map<String, String> parameterMap) {
        this.name = name;
        this.enabled = enabled;
        this.parameterMap = parameterMap;
    }

    /**
     * Deep clone of AuthenticatorConfig.
     *
     * @param authenticatorConfig   authenticatorConfig to be cloned
     */
    public AuthenticatorConfig(AuthenticatorConfig authenticatorConfig) {

        this.name = authenticatorConfig.getName();
        this.applicationAuthenticator = authenticatorConfig.getApplicationAuthenticator();
        this.authenticatorStateInfo = authenticatorConfig.getAuthenticatorStateInfo();
        this.enabled = authenticatorConfig.isEnabled();
        this.idpNames = authenticatorConfig.getIdpNames() != null ?
                new ArrayList<>(authenticatorConfig.getIdpNames()) : null;
        this.idps = authenticatorConfig.getIdps() != null ? new HashMap<>(authenticatorConfig.getIdps()) : null;
        this.parameterMap = authenticatorConfig.getParameterMap() != null ?
                new HashMap<>(authenticatorConfig.getParameterMap()) : null;
        this.idPResourceIds = authenticatorConfig.getIdPResourceIds() != null ?
                new ArrayList<>(authenticatorConfig.getIdPResourceIds()) : null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, String> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public ApplicationAuthenticator getApplicationAuthenticator() {
        return applicationAuthenticator;
    }

    public void setApplicationAuthenticator(
            ApplicationAuthenticator applicationAuthenticator) {
        this.applicationAuthenticator = applicationAuthenticator;
    }

    public AuthenticatorStateInfo getAuthenticatorStateInfo() {
        return authenticatorStateInfo;
    }

    public void setAuthenticatorStateInfo(
            AuthenticatorStateInfo authenticatorStateInfo) {
        this.authenticatorStateInfo = authenticatorStateInfo;
    }

    public List<String> getIdpNames() {
        return idpNames;
    }

    public Map<String, IdentityProvider> getIdps() {
        return idps;
    }

    public void setIdPs(Map<String, IdentityProvider> idPs) {


        if (idPs != null) {
            /* Remove non-serializable UserDefinedAuthenticatorEndpointConfig objects from the
             UserDefinedFederatedAuthenticatorConfig in the context. The UserDefinedAuthenticatorEndpointConfig contains
             the endpoint URI and the authentication type of the corresponding action. However, this information is not
             used in the authentication flow. Instead, the action ID in the authenticator property is used to resolve
             the corresponding action.
             TOD0: https://github.com/wso2/product-is/issues/22907
             [Robust solution for serialization error at authentication context cloning] */
            for (IdentityProvider idp : idPs.values()) {
                for (FederatedAuthenticatorConfig authConfig : idp.getFederatedAuthenticatorConfigs()) {
                    if (authConfig instanceof UserDefinedFederatedAuthenticatorConfig) {
                        ((UserDefinedFederatedAuthenticatorConfig) authConfig).setEndpointConfig(null);
                    }
                }
            }
        }
        this.idps = idPs;
    }

    public void setIdPNames(List<String> idpNames) {

        this.idpNames = idpNames;
    }

    public void setIdPResourceIds(List<String> resourceIds) {

        this.idPResourceIds = resourceIds;
    }

    public List<String> getIdPResourceIds() {

        return this.idPResourceIds;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }
}

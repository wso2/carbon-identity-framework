/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionContextLoaderException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to keep the optimized authenticator config's attributes.
 */
public class OptimizedAuthenticatorConfig implements Serializable {

    private String name;
    private boolean enabled;
    private AuthenticatorStateInfo authenticatorStateInfo;
    private Map<String, String> parameterMap;
    private List<String> idPResourceIds;

    public OptimizedAuthenticatorConfig(AuthenticatorConfig authenticatorConfig) {

        this.name = authenticatorConfig.getName();
        this.enabled = authenticatorConfig.isEnabled();
        this.authenticatorStateInfo = authenticatorConfig.getAuthenticatorStateInfo();
        this.parameterMap = authenticatorConfig.getParameterMap();
        this.idPResourceIds = getIdPResourceIds(authenticatorConfig.getIdps());
    }

    private List<String> getIdPResourceIds(Map<String, IdentityProvider> idps) {

        List<String> idpResourceIds = new ArrayList<>();
        idps.forEach((idpName, idp) -> {
            idpResourceIds.add(idp.getResourceId());
        });
        return idpResourceIds;
    }

    public String getName() {

        return name;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public AuthenticatorStateInfo getAuthenticatorStateInfo() {

        return authenticatorStateInfo;
    }

    public Map<String, String> getParameterMap() {

        return parameterMap;
    }

    public List<String> getIdPResourceIds() {

        return idPResourceIds;
    }

    public AuthenticatorConfig getAuthenticatorConfig(String tenantDomain) throws SessionContextLoaderException {

        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setName(this.name);
        authenticatorConfig.setEnabled(this.enabled);
        authenticatorConfig.setApplicationAuthenticator(FrameworkUtils.getAppAuthenticatorByName(this.name));
        authenticatorConfig.setAuthenticatorStateInfo(this.authenticatorStateInfo);
        authenticatorConfig.setParameterMap(this.parameterMap);
        Map<String, IdentityProvider> idps = new HashMap<>();
        List<String> idpNames = new ArrayList<>();
        for (String resourceId : this.idPResourceIds) {
            IdentityProvider idp = getIdPByResourceID(resourceId, tenantDomain);
            idps.put(idp.getIdentityProviderName(), idp);
            idpNames.add(idp.getIdentityProviderName());
        }
        authenticatorConfig.setIdPs(idps);
        authenticatorConfig.setIdPNames(idpNames);
        return authenticatorConfig;
    }

    private IdentityProvider getIdPByResourceID(String resourceId, String tenantDomain) throws
            SessionContextLoaderException {

        if (resourceId == null) {
            throw new SessionContextLoaderException("Error occurred while getting IdPs");
        }
        IdentityProviderManager manager =
                (IdentityProviderManager) FrameworkServiceDataHolder.getInstance().getIdPManager();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByResourceId(resourceId, tenantDomain, false);
            if (idp == null) {
                throw new SessionContextLoaderException(
                        String.format("Cannot find the Identity Provider by the resource ID: %s " +
                                "tenant domain: %s", resourceId, tenantDomain));
            }
        } catch (IdentityProviderManagementException e) {
            throw new SessionContextLoaderException(
                    String.format("Failed to get the Identity Provider by resource id: %s tenant domain: %s",
                            resourceId, tenantDomain), e);
        }
        return idp;
    }

}

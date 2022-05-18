/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
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
 * This is a wrapper class for ApplicationAuthenticator.
 */
public class AuthenticatorConfig implements Serializable {

    private static final long serialVersionUID = 4391415512399764048L;

    private String name;
    private boolean enabled;
    private AuthenticatorStateInfo authenticatorStateInfo;
    private String tenantDomain;
    private Map<String, String> parameterMap;
    private List<String> idpResourceIDs = new ArrayList<>();
    private List<String> idpNames = new ArrayList<>();

    private static final Log log = LogFactory.getLog(AuthenticatorConfig.class);

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
        this.authenticatorStateInfo = authenticatorConfig.getAuthenticatorStateInfo();
        this.enabled = authenticatorConfig.isEnabled();
        this.tenantDomain = authenticatorConfig.getTenantDomain();
        this.idpNames = authenticatorConfig.getIdpNames() != null ?
                new ArrayList<>(authenticatorConfig.getIdpNames()) : null;
        this.idpResourceIDs = authenticatorConfig.getIdpResourceIDs() != null ?
                new ArrayList<>(authenticatorConfig.getIdpResourceIDs()) : null;
        this.parameterMap = authenticatorConfig.getParameterMap() != null ?
                new HashMap<>(authenticatorConfig.getParameterMap()) : null;
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

        return FrameworkUtils.getAppAuthenticatorByName(this.name);
    }

    @Deprecated
    public void setApplicationAuthenticator(ApplicationAuthenticator applicationAuthenticator) {

        log.warn(String.format(
                "Cannot set the application authenticator: %s in authenticator config", applicationAuthenticator));
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

    public Map<String, IdentityProvider> getIdps() throws FrameworkException {
        return getIdPsFromResourceIdList();
    }

    private Map<String, IdentityProvider> getIdPsFromResourceIdList() throws FrameworkException {

        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        HashMap<String, IdentityProvider> idps = new HashMap<>();
        for (String resourceId : this.idpResourceIDs) {
            try {
                IdentityProvider idp = manager.getIdPByResourceId(resourceId, this.tenantDomain, false);
                if (idp == null) {
                    throw new FrameworkException(
                            String.format("Cannot able to find the IdP for Resource ID: %s and tenant domain: %s",
                                    resourceId, this.tenantDomain));
                }
                idps.put(idp.getIdentityProviderName(), idp);
            } catch (IdentityProviderManagementException e) {
                throw new FrameworkException(
                        String.format("Failed to get the identity provider with resource ID: %s in tenant domain: %s",
                                resourceId, this.tenantDomain), e);
            }
        }
        return idps;
    }

    /**
     * This method is used to add the identity provider's resource id into the list of resource ids by identity provider
     * name.
     *
     * @param idpName identity provider name
     * @param tenantDomain tenant domain
     * @throws FrameworkException Framework Exception
     */
    public void addIdP(String idpName, String tenantDomain) throws FrameworkException {

        this.tenantDomain = tenantDomain;
        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        try {
            IdentityProvider idp = manager.getIdPByName(idpName, this.tenantDomain);
            if (idp == null) {
                throw new FrameworkException(
                        String.format("Cannot able to find the IdP with the name: %s and tenant domain: %s",
                                idpName, tenantDomain));
            }
            this.idpResourceIDs.add(idp.getResourceId());
        } catch (IdentityProviderManagementException e) {
            throw new FrameworkException(
                    String.format("Failed to get the identity provider with name: %s in tenant domain: %s", idpName,
                            tenantDomain), e);
        }
    }

    /**
     * This method is used to remove the identity provider's resource id from the resource id list by providing the
     * name of the idenity provider.
     * @param idpName identity provider.
     * @throws FrameworkException Framework Exception.
     */
    public void removeIdPResourceIdByName(String idpName) throws FrameworkException {

        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByName(idpName, this.tenantDomain);
        } catch (IdentityProviderManagementException e) {
            throw new FrameworkException(
                    String.format("Failed to get the identity provider with name: %s in tenant domain: %s", idpName,
                            tenantDomain), e);
        }
        if (idp == null) {
            throw new FrameworkException(
                    String.format("Cannot able to find the IdP with the name: %s and tenant domain: %s",
                            idpName, tenantDomain));
        }
        this.idpResourceIDs.remove(idp.getResourceId());
    }

    private List<String> getIdpResourceIDs() {

        return this.idpResourceIDs;
    }

    public String getTenantDomain() {

        return this.tenantDomain;
    }
}

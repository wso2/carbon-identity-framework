/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationServerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class keeps the important attributes from the Authenticator Config class.
 * The entire authenticator config can be retrieved back from this class.
 */
public class OptimizedAuthenticatorConfig implements Serializable {

    private static final long serialVersionUID = 6686515855441683088L;

    private final String name;
    private final boolean enabled;
    private final AuthenticatorStateInfo authenticatorStateInfo;
    private final Map<String, String> parameterMap;
    private final List<String> idPResourceIds;
    private final String tenantDomain;

    private static final Log LOG = LogFactory.getLog(OptimizedAuthenticatorConfig.class);

    public OptimizedAuthenticatorConfig(AuthenticatorConfig authenticatorConfig) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Optimization process has started for the authenticator config with the name: "
                    + authenticatorConfig.getName());
        }
        this.name = authenticatorConfig.getName();
        this.enabled = authenticatorConfig.isEnabled();
        this.authenticatorStateInfo = authenticatorConfig.getAuthenticatorStateInfo();
        this.parameterMap = authenticatorConfig.getParameterMap();
        this.tenantDomain = authenticatorConfig.getTenantDomain();
        this.idPResourceIds = authenticatorConfig.getIdPResourceIds();
    }

    public AuthenticatorConfig getAuthenticatorConfig() throws
            SessionDataStorageOptimizationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading process has started for the authenticator config with name: " +
                    this.name);
        }
        AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
        authenticatorConfig.setName(this.name);
        authenticatorConfig.setEnabled(this.enabled);
        authenticatorConfig.setApplicationAuthenticator(FrameworkUtils.getAppAuthenticatorByName(this.name));
        authenticatorConfig.setAuthenticatorStateInfo(this.authenticatorStateInfo);
        authenticatorConfig.setParameterMap(this.parameterMap);
        Map<String, IdentityProvider> idps = new HashMap<>();
        List<String> idpNames = new ArrayList<>();
        for (String resourceId : this.idPResourceIds) {
            IdentityProvider idp = getIdPByResourceID(resourceId, this.tenantDomain);
            idps.put(idp.getIdentityProviderName(), idp);
            idpNames.add(idp.getIdentityProviderName());
        }
        authenticatorConfig.setIdPs(idps);
        authenticatorConfig.setIdPNames(idpNames);
        authenticatorConfig.setTenantDomain(this.tenantDomain);
        return authenticatorConfig;
    }

    private IdentityProvider getIdPByResourceID(String resourceId, String tenantDomain) throws
            SessionDataStorageOptimizationException {

        if (StringUtils.isEmpty(resourceId) || StringUtils.isEmpty(tenantDomain)) {
            throw new SessionDataStorageOptimizationClientException(
                    String.format("Null parameters passed while getting IDPs by the resource ID: %s " +
                    "tenant domain: %s", resourceId, tenantDomain));
        }
        IdentityProviderManager manager =
                (IdentityProviderManager) FrameworkServiceDataHolder.getInstance().getIdPManager();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByResourceId(resourceId, tenantDomain, false);
            if (idp == null) {
                throw new SessionDataStorageOptimizationClientException(
                        String.format("Cannot find the Identity Provider by the resource ID: %s " +
                                "tenant domain: %s", resourceId, tenantDomain));
            }
        } catch (IdentityProviderManagementClientException e) {
            throw new SessionDataStorageOptimizationClientException(
                    String.format("IDP management client error occurred. Failed to get the Identity Provider by " +
                                    "resource id: %s tenant domain: %s", resourceId, tenantDomain), e);
        } catch (IdentityProviderManagementServerException e) {
            throw new SessionDataStorageOptimizationServerException(
                    String.format("IDP management server error occurred. Failed to get the Identity Provider by " +
                                    "resource id: %s tenant domain: %s", resourceId, tenantDomain), e);
        } catch (IdentityProviderManagementException e) {
            throw new SessionDataStorageOptimizationException(
                    String.format("IDP management error occurred. Failed to get the Identity Provider by " +
                                    "resource id: %s tenant domain: %s", resourceId, tenantDomain), e);
        }
        return idp;
    }
}

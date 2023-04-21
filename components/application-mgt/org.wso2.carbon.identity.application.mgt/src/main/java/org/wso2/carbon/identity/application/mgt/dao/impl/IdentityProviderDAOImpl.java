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

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation DAO for Identity provider.
 */
public class IdentityProviderDAOImpl implements IdentityProviderDAO {

    public static final String BASIC = "basic";
    public static final String IWA = "iwa";
    public static final String OAUTH_BEARER = "oauth-bearer";
    public static final String BASIC_AUTH = "basic-auth";
    public static final String SAML_SSO = "samlsso";
    public static final String OPENID_CONNECT = "openidconnect";
    public static final String OPENID = "openid";
    public static final String PASSIVE_STS = "passive-sts";
    public static final String FACEBOOK_AUTH = "facebook";

    /**
     * @param idpName
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public String getDefaultAuthenticator(String idpName)
            throws IdentityApplicationManagementException {
        IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
        try {
            IdentityProvider idp = idpManager.getIdPByName(idpName, CarbonContext
                    .getThreadLocalCarbonContext().getTenantDomain());
            return idp.getDefaultAuthenticatorConfig() != null ? idp
                    .getDefaultAuthenticatorConfig().getName() : null;
        } catch (IdentityProviderManagementException e) {
            throw new IdentityApplicationManagementException("Error when retrieving default authenticator of idp "
                    + idpName, e);
        }
    }

    @Override
    /**
     *
     */
    public IdentityProvider getIdentityProvider(String idpName)
            throws IdentityApplicationManagementException {
        IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
        try {
            IdentityProvider idp = idpManager.getIdPByName(idpName, CarbonContext
                    .getThreadLocalCarbonContext().getTenantDomain());

            IdentityProvider identityProvider = new IdentityProvider();
            identityProvider.setIdentityProviderName(idp.getIdentityProviderName());

            FederatedAuthenticatorConfig defaultAuthenticator = new FederatedAuthenticatorConfig();
            defaultAuthenticator.setName(getDefaultAuthenticator(idp.getIdentityProviderName()));

            List<FederatedAuthenticatorConfig> federatedAuthenticators = new ArrayList<FederatedAuthenticatorConfig>();

            FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = idp
                    .getFederatedAuthenticatorConfigs();
            if (federatedAuthenticatorConfigs != null && federatedAuthenticatorConfigs.length > 0) {
                for (FederatedAuthenticatorConfig config : federatedAuthenticatorConfigs) {
                    if (config.isEnabled()) {
                        federatedAuthenticators.add(config);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(federatedAuthenticators)) {
                identityProvider.setFederatedAuthenticatorConfigs(federatedAuthenticators
                        .toArray(new FederatedAuthenticatorConfig[federatedAuthenticators.size()]));
            }

            List<ProvisioningConnectorConfig> provisioningConnectors = new ArrayList<ProvisioningConnectorConfig>();

            ProvisioningConnectorConfig[] provisioningConnectorConfigs = idp.getProvisioningConnectorConfigs();
            if (provisioningConnectorConfigs != null && provisioningConnectorConfigs.length > 0) {
                for (ProvisioningConnectorConfig config : provisioningConnectorConfigs) {
                    if (config.isEnabled()) {
                        provisioningConnectors.add(config);
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(provisioningConnectors)) {
                identityProvider.setProvisioningConnectorConfigs(provisioningConnectors
                        .toArray(new ProvisioningConnectorConfig[provisioningConnectors.size()]));
            }

            identityProvider.setEnable(idp.isEnable());

            return identityProvider;

        } catch (IdentityProviderManagementException e) {
            throw new IdentityApplicationManagementException("Error when retrieving identity provider " + idpName, e);
        }
    }

    @Override
    /**
     *
     */
    public List<IdentityProvider> getAllIdentityProviders()
            throws IdentityApplicationManagementException {

        IdentityProviderManager idpManager = IdentityProviderManager.getInstance();

        List<IdentityProvider> idps;
        try {
            idps = idpManager
                    .getIdPs(CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        } catch (IdentityProviderManagementException e) {
            throw new IdentityApplicationManagementException("Error when retrieving all identity providers in " +
                    CarbonContext.getThreadLocalCarbonContext().getTenantDomain() + " tenant domain.", e);
        }

        List<IdentityProvider> federatedIdentityProviders = new ArrayList<IdentityProvider>();

        if (idps != null && !idps.isEmpty()) {
            for (IdentityProvider idp : idps) {
                federatedIdentityProviders.add(getIdentityProvider(idp.getIdentityProviderName()));
            }
        }

        return federatedIdentityProviders;
    }

    @Override
    /**
     *
     */
    public List<LocalAuthenticatorConfig> getAllLocalAuthenticators()
            throws IdentityApplicationManagementException {

        return ApplicationAuthenticatorService.getInstance().getLocalAuthenticators();
    }

    @Override
    public ConnectedAppsResult getConnectedAppsOfLocalAuthenticator(String authenticatorId, int tenantId, Integer limit,
                                                                    Integer offset)
            throws IdentityApplicationManagementException {

        IdentityProviderManager idpManager = IdentityProviderManager.getInstance();
        try {
            return idpManager.getConnectedAppsForLocalAuthenticator(authenticatorId, tenantId, limit, offset);
        } catch (IdentityProviderManagementException e) {
            throw new IdentityApplicationManagementException("Error while retrieving connected applications " +
                    "for authenticator Id: " + authenticatorId, e);
        }
    }

    @Override
    /**
     *
     */
    public List<RequestPathAuthenticatorConfig> getAllRequestPathAuthenticators()
            throws IdentityApplicationManagementException {
        return ApplicationAuthenticatorService.getInstance().getRequestPathAuthenticators();

    }

}

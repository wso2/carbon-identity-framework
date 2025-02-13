/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.CacheBackedApplicationDAO;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.listener.AbstractIdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;

/**
 * Internal implementation to listen to IdP CRUD events.
 * Changes the Application/Service Provider data according to IdP changes.
 */
public class ApplicationIdentityProviderMgtListener extends AbstractIdentityProviderMgtListener {

    private static final Log log = LogFactory.getLog(ApplicationIdentityProviderMgtListener.class);

    @Override
    public boolean doPreUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        try {
            IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();

            IdentityProvider oldIdentityProvider = identityProviderManager.getIdPByName(oldIdPName, tenantDomain);
            String idpId = oldIdentityProvider.getResourceId();
            if (identityProvider.getResourceId() == null && idpId != null) {
                identityProvider.setResourceId(idpId);
            }

            if (identityProviderManager.isIdpReferredBySP(idpId, oldIdPName, tenantDomain)) {
                // If the user tries to disable the IDP,Authenticator, or Outbound Connector while it is associated,
                // with at least one SP throw an error.
                validateIdpDisable(identityProvider, tenantDomain);

                // Validating Applications with Federated Authenticators configured.
                updateApplicationWithFederatedAuthenticators(oldIdentityProvider, identityProvider, tenantDomain);

                // Clear application caches if IDP name is updated.
                if (!StringUtils.equals(oldIdPName, identityProvider.getIdentityProviderName())) {
                    int offset = 0;
                    ConnectedAppsResult connectedApplications;
                    do {
                        connectedApplications =
                                identityProviderManager.getConnectedApplications(idpId, null, offset, tenantDomain);

                        for (String appResourceId : connectedApplications.getApps()) {
                            ServiceProvider serviceProvider =
                                    ApplicationMgtSystemConfig.getInstance().getApplicationDAO()
                                            .getApplicationByResourceId(appResourceId, tenantDomain);
                            CacheBackedApplicationDAO.clearAllAppCache(serviceProvider, tenantDomain);
                        }

                        offset = connectedApplications.getOffSet() + connectedApplications.getLimit();

                    } while (connectedApplications.getTotalAppCount() > offset);
                }
            }
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityProviderManagementException(
                    "Error when updating the service providers associated with the IDP " + oldIdPName, e);
        }
        return true;
    }

    /**
     * Update the service providers that have the given IDP as their federated IDP.
     *
     * @param oldIdentityProvider Old identity provider instance.
     * @param newIdentityProvider New updating identity provider instance.
     * @param tenantDomain Tenant domain of Identity Provider.
     * @throws IdentityApplicationManagementException Error when updating applications with federated authenticators.
     */
    private void updateApplicationWithFederatedAuthenticators(IdentityProvider oldIdentityProvider,
                                                              IdentityProvider newIdentityProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        FederatedAuthenticatorConfig oldDefaultAuthenticator = oldIdentityProvider.getDefaultAuthenticatorConfig();
        FederatedAuthenticatorConfig newDefaultAuthenticator = newIdentityProvider.getDefaultAuthenticatorConfig();
        if (oldDefaultAuthenticator != null && newDefaultAuthenticator != null &&
                !StringUtils.equals(oldDefaultAuthenticator.getName(), newDefaultAuthenticator.getName())) {
            ApplicationDAO applicationDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            String[] spResourceIDs = applicationDAO.getSPsAssociatedWithFederatedIDPAuthenticator(
                    oldIdentityProvider.getIdentityProviderName(), oldDefaultAuthenticator.getName(), tenantDomain);

            for (String spResourceId : spResourceIDs) {
                ServiceProvider serviceProvider =
                        applicationDAO.getApplicationByResourceId(spResourceId, tenantDomain);
                IdentityProvider fedIdp = serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                        .getAuthenticationSteps()[0].getFederatedIdentityProviders()[0];
                fedIdp.setDefaultAuthenticatorConfig(newDefaultAuthenticator);
                fedIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[] {newDefaultAuthenticator});
                applicationDAO.updateApplicationLocalAndOutboundAuthConfig(serviceProvider, tenantDomain);
            }
        }
    }

    /**
     * Validate if the current IDP, its authenticator or outbound connector is referenced by a Service Provider and,
     * if in a disabled state, throw an error.
     *
     * @param identityProvider Instance of the identity provider to be validated.
     * @param tenantDomain     Tenant domain of Identity Provider.
     * @throws IdentityProviderManagementException Error when validating IDP/Authenticator/Outbound Connector
     * disable state.
     */
    private void validateIdpDisable(IdentityProvider identityProvider, String tenantDomain)
            throws IdentityProviderManagementException {

        // Verify if the entire IDP is disabled.
        if (!identityProvider.isEnable()) {
            throw new IdentityProviderManagementException(
                    "Error in disabling identity provider as it is referred by service providers.");
        }

        // Verify if any of the referred authenticators is disabled.
        IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();
        FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs =
                identityProvider.getFederatedAuthenticatorConfigs();
        FederatedAuthenticatorConfig defaultAuthenticatorConfig = identityProvider.getDefaultAuthenticatorConfig();
        for (FederatedAuthenticatorConfig federatedAuthenticatorConfig : federatedAuthenticatorConfigs) {
            if (!federatedAuthenticatorConfig.isEnabled() &&
                    identityProviderManager.isAuthenticatorReferredBySP(identityProvider.getResourceId(),
                            identityProvider.getIdentityProviderName(), federatedAuthenticatorConfig.getName(),
                            tenantDomain)) {
                if (defaultAuthenticatorConfig != null && StringUtils.equals(federatedAuthenticatorConfig.getName(),
                        defaultAuthenticatorConfig.getName())) {
                    throw new IdentityProviderManagementException("Error in disabling default federated authenticator" +
                            " as it is referred by service providers.");
                }

                throw new IdentityProviderManagementException(federatedAuthenticatorConfig.getName()
                        + " is referred by service providers.");
            }
        }

        // Verify if any of the referred outbound connector is disabled.
        ProvisioningConnectorConfig[] provisioningConnectorConfigs = identityProvider.getProvisioningConnectorConfigs();
        for (ProvisioningConnectorConfig provisioningConnectorConfig : provisioningConnectorConfigs) {
            if (!provisioningConnectorConfig.isEnabled() &&
                    identityProviderManager.isOutboundConnectorReferredBySP(identityProvider.getResourceId(),
                            identityProvider.getIdentityProviderName(), provisioningConnectorConfig.getName(),
                            tenantDomain)) {
                throw new IdentityProviderManagementClientException(provisioningConnectorConfig.getName() +
                        " connector is already configured for outbound provisioning.");
            }
        }
    }

    @Override
    public boolean doPostDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {

        if (log.isDebugEnabled()) {
            log.debug("doPostDeleteIdp executed for idp: " + idPName + " of tenantDomain: " + tenantDomain);
        }

        // Clear the SP cache since deleted IDP might have contained association with SPs.
        IdentityServiceProviderCache.getInstance().clear(tenantDomain);
        if (log.isDebugEnabled()) {
            log.debug("IdentityServiceProvider Cache is cleared on post delete event of idp: " + idPName + " of " +
                    "tenantDomain: " + tenantDomain);
        }

        return super.doPostDeleteIdP(idPName, tenantDomain);
    }

    /**
     * Additional actions after deleting IdPs of a given tenant id.
     *
     * @param tenantDomain Tenant domain to delete IdPs
     * @return
     * @throws IdentityProviderManagementException
     */
    @Override
    public boolean doPostDeleteIdPs(String tenantDomain) throws IdentityProviderManagementException {

        return super.doPostDeleteIdPs(tenantDomain);
    }

    public int getDefaultOrderId() {

        return 10;
    }
}


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
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.CacheBackedApplicationDAO;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.listener.AbstractIdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;

import java.util.ArrayList;
import java.util.List;

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
            IdentityServiceProviderCache.getInstance().clear(tenantDomain);

            IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();

            ConnectedAppsResult connectedApplications;
            String idpId = identityProviderManager.getIdPByName(oldIdPName, tenantDomain).getResourceId();
            if (identityProvider.getResourceId() == null && idpId != null) {
                identityProvider.setResourceId(idpId);
            }
            int offset = 0;
            do {
                connectedApplications =
                        identityProviderManager.getConnectedApplications(idpId, null, offset, tenantDomain);

                List<ServiceProvider> serviceProvidersList = new ArrayList<>();
                for (String appResourceId : connectedApplications.getApps()) {
                    ServiceProvider serviceProvider = ApplicationMgtSystemConfig.getInstance().getApplicationDAO()
                            .getApplicationByResourceId(appResourceId, tenantDomain);
                    serviceProvidersList.add(serviceProvider);
                }

                for (ServiceProvider serviceProvider : serviceProvidersList) {

                    LocalAndOutboundAuthenticationConfig localAndOutboundAuthConfig = serviceProvider
                            .getLocalAndOutBoundAuthenticationConfig();
                    AuthenticationStep[] authSteps = localAndOutboundAuthConfig.getAuthenticationSteps();

                    OutboundProvisioningConfig outboundProvisioningConfig = serviceProvider
                            .getOutboundProvisioningConfig();
                    IdentityProvider[] provisioningIdps = outboundProvisioningConfig.getProvisioningIdentityProviders();

                    // Check whether the identity provider is referred in a service provider
                    validateIdpDisable(identityProvider, authSteps, provisioningIdps);

                    // Validating Applications with Federated Authenticators configured.
                    updateApplicationWithFederatedAuthenticators(identityProvider, tenantDomain, serviceProvider,
                            localAndOutboundAuthConfig, authSteps);

                    // Validating Applications with Outbound Provisioning Connectors configured.
                    updateApplicationWithProvisioningConnectors(identityProvider, provisioningIdps);

                    // Clear application caches if IDP name is updated.
                    if (!StringUtils.equals(oldIdPName, identityProvider.getIdentityProviderName())) {

                        CacheBackedApplicationDAO.clearAllAppCache(serviceProvider, tenantDomain);
                    }
                }

                offset = connectedApplications.getOffSet() + connectedApplications.getLimit();

            } while (connectedApplications.getTotalAppCount() > offset);

        } catch (IdentityApplicationManagementException e) {
            throw new IdentityProviderManagementException(
                    "Error when updating default authenticator of service providers", e);
        }
        return true;
    }

    @Override
    public boolean doPostUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {

        try {
            IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();
            ConnectedAppsResult connectedApplications;
            String updatedIdpId = identityProvider.getResourceId();
            ApplicationDAO applicationDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            int offset = 0;

            do {
                connectedApplications =
                        identityProviderManager.getConnectedApplications(updatedIdpId, null, offset, tenantDomain);

                for (String appResourceId : connectedApplications.getApps()) {
                    ServiceProvider serviceProvider = applicationDAO.getApplicationByResourceId(appResourceId,
                            tenantDomain);
                    applicationDAO.clearApplicationFromCache(serviceProvider, tenantDomain);
                }

                offset = connectedApplications.getOffSet() + connectedApplications.getLimit();

            } while (connectedApplications.getTotalAppCount() > offset);

        } catch (IdentityApplicationManagementException e) {
            throw new IdentityProviderManagementException("Error while running post IDP update tasks.", e);
        }
        return true;
    }

    private void updateApplicationWithProvisioningConnectors(IdentityProvider identityProvider,
                                                             IdentityProvider[] provisioningIdps)
            throws IdentityProviderManagementException {

        if (provisioningIdps != null && provisioningIdps.length != 0) {
            updateOutboundProvisioningConnectors(identityProvider, provisioningIdps);
        }
    }

    private void updateApplicationWithFederatedAuthenticators(IdentityProvider identityProvider, String tenantDomain,
                                                              ServiceProvider serviceProvider,
                                                              LocalAndOutboundAuthenticationConfig
                                                                      localAndOutboundAuthConfig,
                                                              AuthenticationStep[] authSteps)
            throws IdentityApplicationManagementException, IdentityProviderManagementException {

        if (authSteps != null && authSteps.length != 0) {

            if (ApplicationConstants.AUTH_TYPE_FEDERATED
                    .equalsIgnoreCase(localAndOutboundAuthConfig.getAuthenticationType())) {
                updateApplicationWithFederatedAuthenticator(identityProvider, tenantDomain,
                        serviceProvider, authSteps[0]);
            } else {
                updateApplicationWithMultiStepFederatedAuthenticator(identityProvider, authSteps);
            }
        }
    }

    private void validateIdpDisable(IdentityProvider identityProvider, AuthenticationStep[] authSteps,
                                    IdentityProvider[] provisioningIdps) throws IdentityProviderManagementException {

        if (!identityProvider.isEnable()) {
            for (AuthenticationStep authenticationStep : authSteps) {
                for (IdentityProvider idpProvider : authenticationStep.getFederatedIdentityProviders()) {
                    if (StringUtils.equals(identityProvider.getIdentityProviderName(),
                            idpProvider.getIdentityProviderName())) {
                        throw new IdentityProviderManagementException(
                                "Error in disabling identity provider as it is referred by service providers.");
                    }
                }
            }

            for (IdentityProvider idpProvider : provisioningIdps) {
                if (StringUtils.equals(identityProvider.getIdentityProviderName(),
                        idpProvider.getIdentityProviderName())) {
                    throw new IdentityProviderManagementException(
                            "Error in disabling identity provider as it is referred by service providers.");
                }
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

    /**
     * Check whether the selected authenticator in multi step authentication,
     * is enabled in the updated identity provider.
     *
     * @param identityProvider
     * @param authSteps
     * @throws IdentityProviderManagementException
     */
    private void updateApplicationWithMultiStepFederatedAuthenticator(IdentityProvider identityProvider,
                                                                      AuthenticationStep[] authSteps)
            throws IdentityProviderManagementException {

        FederatedAuthenticatorConfig[] idpFederatedConfig = identityProvider.getFederatedAuthenticatorConfigs();
        for (AuthenticationStep authStep : authSteps) {

            IdentityProvider[] federatedIdentityProviders = authStep.getFederatedIdentityProviders();

            for (IdentityProvider federatedIdp : federatedIdentityProviders) {

                if (StringUtils.equals(federatedIdp.getIdentityProviderName(),
                        identityProvider.getIdentityProviderName())) {
                    FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = federatedIdp
                            .getFederatedAuthenticatorConfigs();
                    String federatedConfigOption = federatedAuthenticatorConfigs[0].getName();

                    for (FederatedAuthenticatorConfig config : idpFederatedConfig) {
                        if (StringUtils.equals(config.getName(),
                                federatedConfigOption) && !config.isEnabled()) {
                            throw new IdentityProviderManagementException(config.getName()
                                    + " is referred by service providers.");
                        }
                    }
                }

            }
        }
    }

    /**
     * Update the service providers, with the default authenticator of the identity provider.
     *
     * @param identityProvider
     * @param tenantDomain
     * @param serviceProvider
     * @param authStep
     * @throws IdentityApplicationManagementException
     * @throws IdentityProviderManagementException
     */
    private void updateApplicationWithFederatedAuthenticator(IdentityProvider identityProvider, String tenantDomain,
                                                             ServiceProvider serviceProvider,
                                                             AuthenticationStep authStep)
            throws IdentityApplicationManagementException, IdentityProviderManagementException {

        IdentityProvider fedIdp = authStep.getFederatedIdentityProviders()[0];
        if (StringUtils.equals(fedIdp.getIdentityProviderName(), identityProvider.getIdentityProviderName())) {

            String defaultAuthName = fedIdp.getDefaultAuthenticatorConfig().getName();
            if (identityProvider.getDefaultAuthenticatorConfig() != null) {
                String currentDefaultAuthName = identityProvider.getDefaultAuthenticatorConfig().getName();
                boolean isCurrentDefaultAuthEnabled = identityProvider.getDefaultAuthenticatorConfig().isEnabled();

                if (!StringUtils.equals(currentDefaultAuthName, defaultAuthName)) {
                    FederatedAuthenticatorConfig currentDefaultAuthenticatorConfig = identityProvider
                            .getDefaultAuthenticatorConfig();
                    fedIdp.setDefaultAuthenticatorConfig(currentDefaultAuthenticatorConfig);
                    fedIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]
                            {currentDefaultAuthenticatorConfig});
                    ApplicationMgtSystemConfig.getInstance().getApplicationDAO()
                            .updateApplication(serviceProvider, tenantDomain);
                } else if (!isCurrentDefaultAuthEnabled && StringUtils.equals(currentDefaultAuthName,
                        defaultAuthName)) {
                    throw new IdentityProviderManagementException("Error in disabling default federated authenticator" +
                            " as it is referred by service providers.");
                }
            } else {
                throw new IdentityProviderManagementException("Error in disabling default federated authenticator" +
                        " as it is referred by service providers.");
            }
        }
    }

    /**
     * Updates Outbound Provisioning Connectors.
     *
     * @param identityProvider
     * @param provisioningIdps
     * @throws IdentityProviderManagementException
     */
    private void updateOutboundProvisioningConnectors(IdentityProvider identityProvider,
                                                      IdentityProvider[] provisioningIdps)
            throws IdentityProviderManagementException {

        ProvisioningConnectorConfig[] idpProvisioningConnectorConfigs =
                identityProvider.getProvisioningConnectorConfigs();
        for (IdentityProvider idpProvider : provisioningIdps) {

            if (StringUtils.equals(idpProvider.getIdentityProviderName(), identityProvider.getIdentityProviderName())) {
                ProvisioningConnectorConfig defaultProvisioningConnectorConfig =
                        idpProvider.getDefaultProvisioningConnectorConfig();

                for (ProvisioningConnectorConfig config : idpProvisioningConnectorConfigs) {
                    if (StringUtils.equals(config.getName(),
                            defaultProvisioningConnectorConfig.getName()) && !config.isEnabled()) {
                        throw new IdentityProviderManagementException(config.getName()
                                + " outbound provisioning connector is referred by service providers.");
                    }
                }
            }
        }
    }
}


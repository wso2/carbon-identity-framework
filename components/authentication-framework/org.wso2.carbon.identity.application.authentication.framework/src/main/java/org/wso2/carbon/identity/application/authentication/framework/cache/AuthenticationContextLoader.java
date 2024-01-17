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

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.OptimizedApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationServerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementServerException;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils.createSPClone;

/**
 * This class is used to optimize the Authentication Context before storing it and again loaded it with objects.
 */
public class AuthenticationContextLoader {

    private static final AuthenticationContextLoader instance = new AuthenticationContextLoader();
    private static final Log LOG = LogFactory.getLog(AuthenticationContextLoader.class);

    private AuthenticationContextLoader() { }

    /**
     * Singleton method.
     * @return Authentication Context Loader.
     */
    public static AuthenticationContextLoader getInstance() {

        return instance;
    }

    /**
     * This method is used to optimize the authentication context object.
     *
     * @param context Authentication context
     * @throws SessionDataStorageOptimizationException Error when optimizing the authenticator configs and
     * application config.
     */
    public void optimizeAuthenticationContext(AuthenticationContext context)
            throws SessionDataStorageOptimizationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Optimization process has started for the authentication context with context id: " +
                    context.getContextIdentifier());
        }
        optimizeExternalIdP(context);
        optimizeAuthenticatorConfig(context);
        optimizeApplicationConfig(context);
    }

    /**
     * This method is used to load the authentication context from optimized authentication context's references.
     *
     * @param context Authentication context
     * @throws SessionDataStorageOptimizationException Error when loading the external idp, authenticator configs,
     * and application config.
     */
    public void loadAuthenticationContext(AuthenticationContext context) throws
            SessionDataStorageOptimizationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Loading process has started for the authentication context with context id: " +
                    context.getContextIdentifier());
        }
        loadExternalIdP(context);
        loadAuthenticatorConfig(context);
        loadApplicationConfig(context);
    }

    private void optimizeExternalIdP(AuthenticationContext context) {

        if (context.getExternalIdP() != null) {
            context.setExternalIdPResourceId(context.getExternalIdP().getIdentityProvider().getResourceId());
        }
        context.setExternalIdP(null);
    }

    private void loadExternalIdP(AuthenticationContext context) throws SessionDataStorageOptimizationException {

        if (context.getExternalIdP() == null && context.getExternalIdPResourceId() != null) {
            IdentityProvider idp = getIdPByResourceID(context.getExternalIdPResourceId(), context.getTenantDomain());
            context.setExternalIdP(new ExternalIdPConfig(idp));
            context.setExternalIdPResourceId(null);
        }
    }

    private void optimizeAuthenticatorConfig(AuthenticationContext context)
            throws SessionDataStorageOptimizationException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        if (sequenceConfig != null) {
            for (Map.Entry<Integer, StepConfig> mapEntry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = mapEntry.getValue();
                if (stepConfig.getAuthenticatedAutenticator() != null) {
                    stepConfig.setAuthenticatedAuthenticatorName(stepConfig.getAuthenticatedAutenticator().getName());
                    stepConfig.setAuthenticatedAutenticator(null);
                }
                List<AuthenticatorConfig> authenticatorList = stepConfig.getAuthenticatorList();
                for (AuthenticatorConfig authenticatorConfig : authenticatorList) {
                    authenticatorConfig.setIdPResourceIds(Collections.emptyList());
                    authenticatorConfig.setApplicationAuthenticator(null);
                    List<String> idPResourceId = new ArrayList<>();
                    for (Map.Entry<String, IdentityProvider> entry : authenticatorConfig.getIdps().entrySet()) {
                        String idpName = entry.getKey();
                        IdentityProvider idp = entry.getValue();
                        if (idp.getResourceId() == null) {
                            idPResourceId.add(getIdPByIdPName(idpName, context.getTenantDomain()).getResourceId());
                        } else {
                            idPResourceId.add(idp.getResourceId());
                        }
                    }
                    authenticatorConfig.setIdPResourceIds(idPResourceId);
                    authenticatorConfig.setTenantDomain(context.getTenantDomain());
                    authenticatorConfig.setIdPs(null);
                    authenticatorConfig.setIdPNames(null);
                }
            }
        }
    }

    private void loadAuthenticatorConfig(AuthenticationContext context)
            throws SessionDataStorageOptimizationException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        if (sequenceConfig != null) {
            for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = entry.getValue();
                for (AuthenticatorConfig authenticatorConfig : stepConfig.getAuthenticatorList()) {
                    if (authenticatorConfig.getApplicationAuthenticator() == null) {
                        authenticatorConfig.setApplicationAuthenticator(FrameworkUtils.
                                getAppAuthenticatorByName(authenticatorConfig.getName()));
                    }
                    if (authenticatorConfig.getIdps() == null && authenticatorConfig.getIdpNames() == null) {
                        authenticatorConfig.setIdPs(Collections.emptyMap());
                        authenticatorConfig.setIdPNames(Collections.emptyList());
                        HashMap<String, IdentityProvider> idPs = new HashMap<>();
                        List<String> idPNames = new ArrayList<>();
                        for (String resourceId : authenticatorConfig.getIdPResourceIds()) {
                            IdentityProvider idp = getIdPByResourceID(resourceId, context.getTenantDomain());
                            idPs.put(idp.getIdentityProviderName(), idp);
                            idPNames.add(idp.getIdentityProviderName());
                        }
                        authenticatorConfig.setIdPs(idPs);
                        authenticatorConfig.setIdPNames(idPNames);
                    }
                }
                if (stepConfig.getAuthenticatedAutenticator() == null &&
                        stepConfig.getAuthenticatedAuthenticatorName() != null) {
                    stepConfig.getAuthenticatorList().forEach(authConfig -> {
                        if (authConfig.getName().equals(stepConfig.getAuthenticatedAuthenticatorName())) {
                            stepConfig.setAuthenticatedAutenticator(authConfig);
                        }
                    });
                }
            }
        }
    }

    private void optimizeApplicationConfig(AuthenticationContext context) throws
            SessionDataStorageOptimizationException {

        if (context.getSequenceConfig() != null) {
            ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
            if (applicationConfig != null) {
                OptimizedApplicationConfig optimizedApplicationConfig =
                        new OptimizedApplicationConfig(applicationConfig, context.getTenantDomain());
                context.getSequenceConfig().setOptimizedApplicationConfig(optimizedApplicationConfig);
                context.getSequenceConfig().setApplicationConfig(null);
            }
        }
    }

    private void loadApplicationConfig(AuthenticationContext context)
            throws SessionDataStorageOptimizationException {

        if (context.getSequenceConfig() != null) {
            ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
            OptimizedApplicationConfig optimizedApplicationConfig = context.getSequenceConfig().
                    getOptimizedApplicationConfig();
            if (applicationConfig == null && optimizedApplicationConfig != null) {
                ServiceProvider serviceProvider = reconstructServiceProvider(optimizedApplicationConfig,
                        context.getTenantDomain());
                ApplicationConfig appConfig = new ApplicationConfig(serviceProvider, context.getTenantDomain());
                appConfig.setMappedSubjectIDSelected(optimizedApplicationConfig.isMappedSubjectIDSelected());
                appConfig.setClaimMappings(optimizedApplicationConfig.getClaimMappings());
                appConfig.setRoleMappings(optimizedApplicationConfig.getRoleMappings());
                appConfig.setMandatoryClaims(optimizedApplicationConfig.getMandatoryClaims());
                appConfig.setRequestedClaims(optimizedApplicationConfig.getRequestedClaims());
                context.getSequenceConfig().setApplicationConfig(appConfig);
            }
        }
    }

    private ServiceProvider reconstructServiceProvider(OptimizedApplicationConfig optimizedApplicationConfig,
                                                       String tenantDomain)
            throws SessionDataStorageOptimizationException {

        ServiceProvider serviceProvider;
        ServiceProvider clonedSP;
        ApplicationManagementServiceImpl applicationManager = (ApplicationManagementServiceImpl)
                FrameworkServiceDataHolder.getInstance().getApplicationManagementService();
        try {
            serviceProvider = applicationManager.getApplicationByResourceId(
                    optimizedApplicationConfig.getServiceProviderResourceId(), tenantDomain);
            if (serviceProvider == null) {
                throw new SessionDataStorageOptimizationClientException(
                        String.format("Cannot find the Service Provider by the resource ID: %s tenant domain: %s",
                                optimizedApplicationConfig.getServiceProviderResourceId(), tenantDomain));
            }
            if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() == null) {
                throw new SessionDataStorageOptimizationClientException(
                        String.format("Cannot find the LocalAndOutBoundAuthenticationConfig for the " +
                                        "Service Provider by the resource ID: %s tenant domain: %s",
                                optimizedApplicationConfig.getServiceProviderResourceId(), tenantDomain));
            }
            clonedSP = createSPClone(serviceProvider);
            clonedSP.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    optimizedApplicationConfig.getAuthenticationSteps(tenantDomain));
        } catch (IdentityApplicationManagementClientException e) {
            throw new SessionDataStorageOptimizationClientException(
                    String.format("Application management client error occurred while retrieving the service provider" +
                                    " by resource id: %s tenant domain: %s",
                            optimizedApplicationConfig.getServiceProviderResourceId(), tenantDomain), e);
        } catch (IdentityApplicationManagementServerException | FrameworkException e) {
            throw new SessionDataStorageOptimizationServerException(
                    String.format("Server occurred while retrieving the service provider by resource id: %s tenant " +
                                    "domain: %s", optimizedApplicationConfig.getServiceProviderResourceId(),
                            tenantDomain), e);
        } catch (IdentityApplicationManagementException e) {
            throw new SessionDataStorageOptimizationException(
                    String.format("Error occurred while retrieving the service provider by resource id: %s tenant " +
                            "domain: %s", optimizedApplicationConfig.getServiceProviderResourceId(), tenantDomain), e);
        }
        return clonedSP;
    }

    private IdentityProvider getIdPByIdPName(String idPName, String tenantDomain)
            throws SessionDataStorageOptimizationException {

        IdentityProviderManager manager =
                (IdentityProviderManager) FrameworkServiceDataHolder.getInstance().getIdentityProviderManager();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByName(idPName, tenantDomain);
            if (idp == null) {
                throw new SessionDataStorageOptimizationClientException(String.format(
                        "Cannot find the Identity Provider by the name: %s tenant domain: %s", idPName, tenantDomain));
            }
        } catch (IdentityProviderManagementClientException e) {
            throw new SessionDataStorageOptimizationClientException(String.format(
                    "IDP management client exception caught. Failed to get the Identity Provider " +
                            "by name: %s tenant domain: %s", idPName, tenantDomain), e);
        } catch (IdentityProviderManagementServerException e) {
            throw new SessionDataStorageOptimizationServerException(String.format(
                    "IDP management server exception caught. Failed to get the Identity Provider " +
                            "by name: %s tenant domain: %s", idPName, tenantDomain), e);
        } catch (IdentityProviderManagementException e) {
            throw new SessionDataStorageOptimizationServerException(String.format(
                    "Failed to get the Identity Provider by name: %s tenant domain: %s", idPName, tenantDomain), e);
        }
        return idp;
    }

    private IdentityProvider getIdPByResourceID(String resourceId, String tenantDomain)
            throws SessionDataStorageOptimizationException {

        IdentityProviderManager manager =
                (IdentityProviderManager) FrameworkServiceDataHolder.getInstance().getIdentityProviderManager();
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
                    String.format("IDP management client error. Failed to get the Identity Provider by " +
                                    "resource id: %s tenant domain: %s", resourceId, tenantDomain), e);
        } catch (IdentityProviderManagementServerException e) {
            throw new SessionDataStorageOptimizationServerException(
                    String.format("IDP management server error. Failed to get the Identity Provider by " +
                                    "resource id: %s tenant domain: %s", resourceId, tenantDomain), e);
        } catch (IdentityProviderManagementException e) {
            throw new SessionDataStorageOptimizationServerException(
                    String.format("Failed to get the Identity Provider by resource id: %s tenant domain: %s",
                            resourceId, tenantDomain), e);
        }
        return idp;
    }
}

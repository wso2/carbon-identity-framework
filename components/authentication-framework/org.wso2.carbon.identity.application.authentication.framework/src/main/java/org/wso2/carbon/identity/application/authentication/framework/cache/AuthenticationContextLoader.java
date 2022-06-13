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

package org.wso2.carbon.identity.application.authentication.framework.cache;

import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.OptimizedApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationContextLoaderException;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to optimize the Authentication Context before storing it and again loaded it with objects.
 */
public class AuthenticationContextLoader {

    private static volatile AuthenticationContextLoader instance;

    private AuthenticationContextLoader() { }

    /**
     * Singleton method.
     * @return Authentication Context Loader
     */
    public static AuthenticationContextLoader getInstance() {

        if (instance == null) {
            synchronized (AuthenticationContextLoader.class) {
                if (instance == null) {
                    instance = new AuthenticationContextLoader();
                }
            }
        }
        return instance;
    }

    /**
     * This method is used to optimize the authentication context object.
     * @param context Authentication context
     * @throws AuthenticationContextLoaderException Authentication Context Loader Exception
     */
    public void optimizeAuthenticationContext(AuthenticationContext context)
            throws AuthenticationContextLoaderException {

        optimizeExternalIdP(context);
        optimizeAuthenticatorConfig(context);
        optimizeApplicationConfig(context);
    }

    /**
     * This method is used to load the authentication context from optimized authentication context's references.
     * @param context Authentication context
     * @throws AuthenticationContextLoaderException Authentication Context Loader Exception
     */
    public void loadAuthenticationContext(AuthenticationContext context) throws
            AuthenticationContextLoaderException {

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

    private void loadExternalIdP(AuthenticationContext context) throws AuthenticationContextLoaderException {

        if (context.getExternalIdP() == null && context.getExternalIdPResourceId() != null) {
            IdentityProvider idp = getIdPsByResourceID(context.getExternalIdPResourceId(), context.getTenantDomain());
            context.setExternalIdP(new ExternalIdPConfig(idp));
            context.setExternalIdPResourceId(null);
        }
    }

    private void optimizeAuthenticatorConfig(AuthenticationContext context)
            throws AuthenticationContextLoaderException {

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
                    authenticatorConfig.setIdPResourceIds(new ArrayList<>());
                    authenticatorConfig.setApplicationAuthenticator(null);
                    List<String> idPResourceId = new ArrayList<>();
                    for (Map.Entry<String, IdentityProvider> entry : authenticatorConfig.getIdps().entrySet()) {
                        String idpName = entry.getKey();
                        IdentityProvider idp = entry.getValue();
                        if (idp.getResourceId() == null) {
                            idPResourceId.add(getIdPsByIdPName(idpName, context.getTenantDomain()).getResourceId());
                        } else {
                            idPResourceId.add(idp.getResourceId());
                        }
                    }
                    authenticatorConfig.setIdPResourceIds(idPResourceId);
                    authenticatorConfig.setIdPs(null);
                    authenticatorConfig.setIdPNames(null);
                }
            }
        }
    }

    private void loadAuthenticatorConfig(AuthenticationContext context)
            throws AuthenticationContextLoaderException {

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
                        authenticatorConfig.setIdPs(new HashMap<>());
                        authenticatorConfig.setIdPNames(new ArrayList<>());
                        HashMap<String, IdentityProvider> idPs = new HashMap<>();
                        List<String> idPNames = new ArrayList<>();
                        for (String resourceId : authenticatorConfig.getIdPResourceIds()) {
                            IdentityProvider idp = getIdPsByResourceID(resourceId, context.getTenantDomain());
                            idPs.put(idp.getIdentityProviderName(), idp);
                            idPNames.add(idp.getIdentityProviderName());
                        }
                        authenticatorConfig.setIdPs(idPs);
                        authenticatorConfig.setIdPNames(idPNames);
                    }
                }
                if (stepConfig.getAuthenticatedAutenticator() == null) {
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
            AuthenticationContextLoaderException {

        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
        if (applicationConfig != null) {
            OptimizedApplicationConfig optApplicationConfig = new OptimizedApplicationConfig(applicationConfig,
                    context.getTenantDomain());
            context.getSequenceConfig().setOptApplicationConfig(optApplicationConfig);
            context.getSequenceConfig().setApplicationConfig(null);
        }
    }

    private void loadApplicationConfig(AuthenticationContext context)
            throws AuthenticationContextLoaderException {

        ApplicationConfig applicationConfig = context.getSequenceConfig().getApplicationConfig();
        OptimizedApplicationConfig optApplicationConfig = context.getSequenceConfig().getOptApplicationConfig();
        if (applicationConfig == null && optApplicationConfig != null) {
            ServiceProvider serviceProvider = reconstructServiceProvider(optApplicationConfig,
                    context.getTenantDomain());
            if (serviceProvider == null) {
                throw new AuthenticationContextLoaderException(
                        String.format("Cannot find the Service Provider by the resource ID: %s tenant domain: %s",
                                optApplicationConfig.getServiceProviderResourceId(), context.getTenantDomain()));
            }
            ApplicationConfig appConfig = new ApplicationConfig(serviceProvider, context.getTenantDomain());
            appConfig.setMappedSubjectIDSelected(optApplicationConfig.isMappedSubjectIDSelected());
            appConfig.setClaimMappings(optApplicationConfig.getClaimMappings());
            appConfig.setRoleMappings(optApplicationConfig.getRoleMappings());
            appConfig.setMandatoryClaims(optApplicationConfig.getMandatoryClaims());
            appConfig.setRequestedClaims(optApplicationConfig.getRequestedClaims());
            context.getSequenceConfig().setApplicationConfig(appConfig);
        }
    }

    private ServiceProvider reconstructServiceProvider(OptimizedApplicationConfig optApplicationConfig,
                                                       String tenantDomain)
            throws AuthenticationContextLoaderException {

        ServiceProvider serviceProvider;
        try {
            serviceProvider = ApplicationMgtSystemConfig.getInstance().getApplicationDAO().
                    getApplicationByResourceId(optApplicationConfig.getServiceProviderResourceId(), tenantDomain);
            if (serviceProvider == null) {
                return null;
            }
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    optApplicationConfig.getAuthenticationSteps(tenantDomain));
        } catch (IdentityApplicationManagementException | FrameworkException e) {
            throw new AuthenticationContextLoaderException(
                    String.format("Error occurred while retrieving the service provider by resource id: %s " +
                            "tenant domain: %s", optApplicationConfig.getServiceProviderResourceId(), tenantDomain), e);
        }
        return serviceProvider;
    }

    private IdentityProvider getIdPsByIdPName(String idPName, String tenantDomain)
            throws AuthenticationContextLoaderException {

        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByName(idPName, tenantDomain);
            if (idp == null) {
                throw new AuthenticationContextLoaderException(String.format(
                        "Cannot find the Identity Provider by the name: %s tenant domain: %s", idPName, tenantDomain));
            }
        } catch (IdentityProviderManagementException e) {
            throw new AuthenticationContextLoaderException(String.format(
                    "Failed to get the Identity Provider by name: %s tenant domain: %s", idPName, tenantDomain), e);
        }
        return idp;
    }

    private IdentityProvider getIdPsByResourceID(String resourceId, String tenantDomain)
            throws AuthenticationContextLoaderException {

        if (resourceId == null) {
            throw new AuthenticationContextLoaderException("Error occurred while getting IdPs");
        }
        IdentityProviderManager manager = IdentityProviderManager.getInstance();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByResourceId(resourceId, tenantDomain, false);
            if (idp == null) {
                throw new AuthenticationContextLoaderException(
                        String.format("Cannot find the Identity Provider by the resource ID: %s " +
                                "tenant domain: %s", resourceId, tenantDomain));
            }
        } catch (IdentityProviderManagementException e) {
            throw new AuthenticationContextLoaderException(
                    String.format("Failed to get the Identity Provider by resource id: %s tenant domain: %s",
                            resourceId, tenantDomain), e);
        }
        return idp;
    }
}

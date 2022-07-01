/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.loader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sequence Configuration loader, loads the sequence configuration from the database.
 * <p>
 * History: The main logic was moved from @see
 * {@link org.wso2.carbon.identity.application.authentication.framework.config.builder.UIBasedConfigurationBuilder},
 * This is one step to move away from Singleton pattern used throughout the code.
 * Few other singletons should be removed and passed relevant information as setters or constructor arguments here.
 */
public class UIBasedConfigurationLoader implements SequenceLoader {

    private static final Log log = LogFactory.getLog(UIBasedConfigurationLoader.class);

    @Override
    public SequenceConfig getSequenceConfig(AuthenticationContext context, Map<String, String[]> parameterMap,
                                            ServiceProvider serviceProvider) throws FrameworkException {

        String tenantDomain = context.getTenantDomain();

        AuthenticationStep[] authenticationSteps = null;

        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = serviceProvider
                .getLocalAndOutBoundAuthenticationConfig();
        if (localAndOutboundAuthenticationConfig.getAuthenticationSteps() != null
                && localAndOutboundAuthenticationConfig.getAuthenticationSteps().length > 0) {
            //Use the default steps when there are no chains configured.
            authenticationSteps = localAndOutboundAuthenticationConfig.getAuthenticationSteps();
        }

        SequenceConfig sequenceConfig = getSequence(serviceProvider, tenantDomain, authenticationSteps);

        //Use script based evaluation if script is present.
        if (isAuthenticationScriptBasedSequence(localAndOutboundAuthenticationConfig)) {
            //Clear the sequenceConfig step map, so that it will be re-populated by Dynamic execution
            Map<Integer, StepConfig> originalStepConfigMap = new HashMap<>(sequenceConfig.getStepMap());
            Map<Integer, StepConfig> stepConfigMapCopy = new HashMap<>();
            originalStepConfigMap.forEach((k, v) -> stepConfigMapCopy.put(k, new StepConfig(v)));
            sequenceConfig.getStepMap().clear();
            JsGraphBuilderFactory jsGraphBuilderFactory = FrameworkServiceDataHolder.getInstance()
                    .getJsGraphBuilderFactory();
            JsGraphBuilder jsGraphBuilder = jsGraphBuilderFactory.createBuilder(context, stepConfigMapCopy);
            context.setServiceProviderName(serviceProvider.getApplicationName());

            AuthenticationGraph graph = jsGraphBuilder
                    .createWith(localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig().getContent())
                    .build();
            graph.setEnabled(localAndOutboundAuthenticationConfig.getAuthenticationScriptConfig().isEnabled());
            sequenceConfig.setAuthenticationGraph(graph);
            graph.setStepMap(originalStepConfigMap);
        }
        return sequenceConfig;
    }

    private boolean isAuthenticationScriptBasedSequence(LocalAndOutboundAuthenticationConfig
                                                                localAndOutboundAuthenticationConfig) {

        if (ApplicationConstants.AUTH_TYPE_FLOW.equals(localAndOutboundAuthenticationConfig.getAuthenticationType()) ||
                ApplicationConstants.AUTH_TYPE_DEFAULT.equals(
                        localAndOutboundAuthenticationConfig.getAuthenticationType())) {
            AuthenticationScriptConfig authenticationScriptConfig = localAndOutboundAuthenticationConfig
                    .getAuthenticationScriptConfig();
            return authenticationScriptConfig != null && authenticationScriptConfig.isEnabled();
        }
        return false;
    }

    /**
     * This is maintained for backward compatibility and should be called form UIBasedConfigurationBuilder only.
     *
     * @param serviceProvider
     * @param tenantDomain
     * @return
     * @throws FrameworkException
     * @see org.wso2.carbon.identity.application.authentication.framework.config.builder.UIBasedConfigurationBuilder
     * @deprecated Please do not use this for any development as this is maintained for backward compatibility.
     */
    @Deprecated
    public SequenceConfig getSequence(ServiceProvider serviceProvider, String tenantDomain) throws FrameworkException {

        if (serviceProvider == null) {
            throw new FrameworkException("ServiceProvider cannot be null");
        }
        AuthenticationStep[] authenticationSteps = serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                .getAuthenticationSteps();

        return getSequence(serviceProvider, tenantDomain, authenticationSteps);
    }

    /**
     * Loads the sequence in the way previous loading mechanism used to work.
     * Please do not use this for any new development.
     *
     * @param serviceProvider
     * @param tenantDomain
     * @param authenticationSteps
     * @return
     * @throws FrameworkException
     */
    public SequenceConfig getSequence(ServiceProvider serviceProvider, String tenantDomain,
                                      AuthenticationStep[] authenticationSteps) throws FrameworkException {

        if (serviceProvider == null) {
            throw new FrameworkException("ServiceProvider cannot be null");
        }
        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setApplicationId(serviceProvider.getApplicationName());
        sequenceConfig.setApplicationConfig(new ApplicationConfig(serviceProvider, tenantDomain));

        // setting request path authenticators
        loadRequestPathAuthenticators(sequenceConfig, serviceProvider);

        int stepOrder = 0;

        if (authenticationSteps == null) {
            return sequenceConfig;
        }

        // for each configured step
        for (AuthenticationStep authenticationStep : authenticationSteps) {

            try {
                stepOrder = authenticationStep.getStepOrder();
            } catch (NumberFormatException e) {
                stepOrder++;
            }

            // create a step configuration object
            StepConfig stepConfig = createStepConfigurationObject(stepOrder, authenticationStep);

            // loading Federated Authenticators
            loadFederatedAuthenticators(authenticationStep, stepConfig, tenantDomain);

            // loading local authenticators
            loadLocalAuthenticators(authenticationStep, stepConfig);

            sequenceConfig.getStepMap().put(stepOrder, stepConfig);
        }

        return sequenceConfig;
    }

    protected StepConfig createStepConfigurationObject(int stepOrder, AuthenticationStep authenticationStep) {

        StepConfig stepConfig = new StepConfig();
        stepConfig.setOrder(stepOrder);
        stepConfig.setSubjectAttributeStep(authenticationStep.isAttributeStep());
        stepConfig.setSubjectIdentifierStep(authenticationStep.isSubjectStep());
        return stepConfig;
    }

    protected void loadRequestPathAuthenticators(SequenceConfig sequenceConfig, ServiceProvider serviceProvider) {

        if (serviceProvider.getRequestPathAuthenticatorConfigs() != null
                && serviceProvider.getRequestPathAuthenticatorConfigs().length > 0) {

            List<AuthenticatorConfig> requestPathAuthenticators = new ArrayList<AuthenticatorConfig>();
            RequestPathAuthenticatorConfig[] reqAuths = serviceProvider.getRequestPathAuthenticatorConfigs();

            // for each request path authenticator
            for (RequestPathAuthenticatorConfig reqAuth : reqAuths) {

                AuthenticatorConfig authConfig = new AuthenticatorConfig();
                String authenticatorName = reqAuth.getName();
                authConfig.setName(authenticatorName);
                authConfig.setEnabled(true);

                // iterate through each system authentication config
                for (ApplicationAuthenticator appAuthenticator : FrameworkServiceComponent.getAuthenticators()) {

                    if (authenticatorName.equalsIgnoreCase(appAuthenticator.getName())) {
                        authConfig.setApplicationAuthenticator(appAuthenticator);
                        break;
                    }
                }
                requestPathAuthenticators.add(authConfig);
            }

            sequenceConfig.setReqPathAuthenticators(requestPathAuthenticators);
        }
    }

    protected void loadFederatedAuthenticators(AuthenticationStep authenticationStep, StepConfig stepConfig,
                                               String tenantDomain) throws FrameworkException {

        IdentityProvider[] federatedIDPs = authenticationStep.getFederatedIdentityProviders();

        if (federatedIDPs != null) {
            // for each idp in the step
            for (IdentityProvider federatedIDP : federatedIDPs) {

                FederatedAuthenticatorConfig federatedAuthenticator = federatedIDP.getDefaultAuthenticatorConfig();

                //When loading the federated IDP configuration from default.xml file in service-providers, we need to
                // retrieve the federated IDP and load
                if (federatedAuthenticator == null) {
                    try {
                        federatedAuthenticator = IdentityProviderManager.getInstance()
                                .getIdPByName(federatedIDP.getIdentityProviderName(), tenantDomain)
                                .getDefaultAuthenticatorConfig();
                    } catch (IdentityProviderManagementException e) {
                        throw new FrameworkException(
                                "Failed to load the default authenticator for IDP : " + federatedIDP
                                        .getIdentityProviderName(), e);
                    }
                }

                String actualAuthenticatorName = federatedAuthenticator.getName();
                // assign it to the step
                loadStepAuthenticator(stepConfig, federatedIDP, actualAuthenticatorName);
            }
        }
    }

    protected void loadLocalAuthenticators(AuthenticationStep authenticationStep, StepConfig stepConfig) {

        LocalAuthenticatorConfig[] localAuthenticators = authenticationStep.getLocalAuthenticatorConfigs();
        if (localAuthenticators != null) {
            IdentityProvider localIdp = new IdentityProvider();
            localIdp.setIdentityProviderName(FrameworkConstants.LOCAL_IDP_NAME);
            // assign it to the step
            for (LocalAuthenticatorConfig localAuthenticator : localAuthenticators) {
                String actualAuthenticatorName = localAuthenticator.getName();
                loadStepAuthenticator(stepConfig, localIdp, actualAuthenticatorName);
            }
        }
    }

    private void loadStepAuthenticator(StepConfig stepConfig, IdentityProvider idp, String authenticatorName) {

        AuthenticatorConfig authenticatorConfig = null;

        // check if authenticator already exists
        for (AuthenticatorConfig authConfig : stepConfig.getAuthenticatorList()) {

            if (authenticatorName.equals(authConfig.getName())) {
                authenticatorConfig = authConfig;
                break;
            }
        }

        if (authenticatorConfig == null) {
            authenticatorConfig = new AuthenticatorConfig();
            authenticatorConfig.setName(authenticatorName);

            for (ApplicationAuthenticator appAuthenticator : FrameworkServiceComponent.getAuthenticators()) {

                if (authenticatorName.equalsIgnoreCase(appAuthenticator.getName())) {
                    authenticatorConfig.setApplicationAuthenticator(appAuthenticator);
                    break;
                }
            }

            stepConfig.getAuthenticatorList().add(authenticatorConfig);
        }

        if (idp != null) {
            authenticatorConfig.getIdpNames().add(idp.getIdentityProviderName());
            authenticatorConfig.getIdps().put(idp.getIdentityProviderName(), idp);
        }

        if (!stepConfig.isMultiOption() && (stepConfig.getAuthenticatorList().size() > 1
                || authenticatorConfig.getIdps().size() > 1)) {
            stepConfig.setMultiOption(true);
        }
    }
}

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

package org.wso2.carbon.identity.application.authentication.framework.config.builder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationStepsSelector;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.AuthenticationChainConfig;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;

import java.util.List;

/**
 * Default implementation of the  AdaptiveAuthenticationEvaluator. This uses the "authenticationClasses" parameter
 * of the Authenticator to decide that this step is applicable on the current request.
 * The ACR level mapping is done similar to,
 * LEVEL0=*
 * LEVEL1=pwd
 * LEVEL2=pwd, iwa
 */
public class DefaultAuthStepsSelector implements AuthenticationStepsSelector {

    private static final Log log = LogFactory.getLog(DefaultAuthStepsSelector.class);


    @Override
    public boolean isAuthenticationSatisfied(AuthenticatorConfig authenticatorConfig, AuthenticationContext context) {
        return false;
    }

    @Override
    public AuthenticationChainConfig resolveSequenceChain(AuthenticationContext context,
            ServiceProvider serviceProvider) {
        if (context.getAcrRequested() == null || context.getAcrRequested().size() <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("No ACR values are present in the context. ACR based chain selection will not happen.");
            }
            return null;
        }

        if (serviceProvider == null) {
            throw new IllegalArgumentException("Service Provider passed to the method is null");
        }
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = serviceProvider
                .getLocalAndOutBoundAuthenticationConfig();
        AuthenticationChainConfig[] authenticationChainConfigs = localAndOutboundAuthenticationConfig
                .getAuthenticationChainConfigs();

        if (authenticationChainConfigs == null || authenticationChainConfigs.length <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("No multiple authentication chains defined for the service provider: " + serviceProvider
                        .getApplicationName() + " . ACR based chain selection will not happen.");
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("ACR based chain selection for the service provider: " + serviceProvider.getApplicationName()
                    + ", Requested acr_values: " + context.getAcrRequested());
        }
        AuthenticationChainConfig bestMatch = null;
        int matchLevelX = 0;
        int matchLevelY = 0;
        List<String> acrListRequested = context.getAcrRequested();

        for (AuthenticationChainConfig chainConfig : authenticationChainConfigs) {
            String[] acrFromChain = chainConfig.getAcr();
            int y = 0;
            for (String acrChecked : acrListRequested) {
                for (int x = 0; x < acrFromChain.length; x++) {
                    if ((bestMatch == null || distance(matchLevelX, matchLevelY) > distance(x, y)) && acrFromChain[x]
                            .equals(acrChecked)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Reassigning Best Match for the authentication chain: " + chainConfig.getName());
                        }
                        bestMatch = chainConfig;
                        matchLevelX = x;
                        matchLevelY = y;
                    }
                }
                y++;
            }
        }
        return bestMatch;
    }

    /*
     * Calculate geometric distance of two coordinates in order to compare best match for ACR list comparison.
     */
    private long distance(int x, int y) {
        return x * x + y * y;
    }

    private boolean isApplicableWithAmr(StepConfig stepConfig, List<String> amrList, AuthenticationContext context) {
        List<AuthenticatorConfig> authenticatorConfigList = stepConfig.getAuthenticatorList();
        if (authenticatorConfigList == null || authenticatorConfigList.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Authenticator list for this step is empty. The AMR is treated not applicable. Step : "
                        + stepConfig.getOrder() + " , AMR list : " + amrList);
            }
            return false;
        }
        for (AuthenticatorConfig authenticatorConfig : authenticatorConfigList) {
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();
            if (authenticator == null) {
                if (log.isDebugEnabled()) {
                    log.debug("The Authenticator is null at the config. Step : " + stepConfig.getOrder()
                            + " , AMR list : " + amrList + ", Authenticator Config Name : " + authenticatorConfig
                            .getName());
                }
            } else {
                if (authenticator.getName() == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("The Authenticator name is null at the config. Step : " + stepConfig.getOrder()
                                + ", Authenticator Config Name : " + authenticatorConfig.getName());
                    }
                }
                if (amrList.contains(authenticator.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isApplicableWithAcr(StepConfig stepConfig, List<String> acrList, AuthenticationContext context) {
        List<AuthenticatorConfig> authenticatorConfigList = stepConfig.getAuthenticatorList();
        if (acrList != null && !acrList.isEmpty()) {
            switch (context.getAcrRule()) {
            case EXACT:
                return isApplicableExact(authenticatorConfigList, acrList);
            default:
                return isApplicableExact(authenticatorConfigList, acrList);
            }
        }
        return false;
    }

    private boolean isApplicableExact(List<AuthenticatorConfig> authenticatorConfigList, List<String> acrList) {
        return isApplicable(authenticatorConfigList, acrList);
    }

    private boolean isApplicable(List<AuthenticatorConfig> authenticatorConfigList, List<String> acrList) {
        for (AuthenticatorConfig authenticatorConfig : authenticatorConfigList) {
            if (isApplicable(authenticatorConfig, acrList)) {
                return true;
            }
        }
        return false;
    }

    private boolean isApplicable(AuthenticatorConfig authenticatorConfig, List<String> acrList) {
        for (String acr : acrList) {
            if (isApplicable(authenticatorConfig, acr)) {
                return true;
            }
        }
        return false;
    }

    private boolean isApplicable(AuthenticatorConfig authenticatorConfig, String acr) {
        Object authClasses = authenticatorConfig.getParameterMap().get("authenticationClasses");
        if (authClasses == null) {
            return true;
        }
        if (authClasses.equals(acr)) {
            return true;
        }
        return false;
    }
}

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

package org.wso2.carbon.identity.application.authentication.framework.context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.OptimizedSequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.OptimizedAuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to store the session context as the optimized one.
 */
public class OptimizedSessionContext implements Serializable {

    private final Map<String, OptimizedSequenceConfig> optimizedAuthenticatedSequences;
    private final Map<String, OptimizedAuthenticatedIdPData> optimizedAuthenticatedIdPs;
    private final String tenantDomain;
    private final boolean isRememberMe;
    private final Map<String, Object> properties;
    private final SessionAuthHistory sessionAuthHistory;
    private final Map<String, Map<String, OptimizedAuthenticatedIdPData>> optimizedAuthenticatedIdPsOfApp;

    private static final Log log = LogFactory.getLog(OptimizedSessionContext.class);

    public OptimizedSessionContext(SessionContext sessionContext) throws SessionDataStorageOptimizationException {

        Object authUser = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
        // TODO : Need to check whether this is the correct way to get the tenant domain.
        this.tenantDomain = authUser instanceof AuthenticatedUser ?
                ((AuthenticatedUser) authUser).getTenantDomain() : null;
        this.optimizedAuthenticatedSequences = getOptimizedAuthenticatedSequences(
                sessionContext.getAuthenticatedSequences());
        this.optimizedAuthenticatedIdPs = getOptimizedAuthenticatedIdPs(sessionContext.getAuthenticatedIdPs());
        this.isRememberMe = sessionContext.isRememberMe();
        this.properties = sessionContext.getProperties();
        this.sessionAuthHistory = sessionContext.getSessionAuthHistory();
        this.optimizedAuthenticatedIdPsOfApp = getOptimizedAuthenticatedIdPsOfApp(sessionContext.
                getAuthenticatedIdPsOfApp());
        if (log.isDebugEnabled()) {
            log.debug("Optimization process for the session context is completed.");
        }
    }

    private Map<String, OptimizedSequenceConfig> getOptimizedAuthenticatedSequences
            (Map<String, SequenceConfig> authenticatedSequences) throws SessionDataStorageOptimizationException {

        Map<String, OptimizedSequenceConfig> optimizedAuthenticatedSequences = new HashMap<>();
        for (Map.Entry<String, SequenceConfig> entry : authenticatedSequences.entrySet()) {
            SequenceConfig sequenceConfig = entry.getValue();
            for (Map.Entry<Integer, StepConfig> mapEntry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = mapEntry.getValue();
                List<AuthenticatorConfig> authenticatorList = stepConfig.getAuthenticatorList();
                for (AuthenticatorConfig authenticatorConfig : authenticatorList) {
                    List<String> idPResourceId = new ArrayList<>();
                    for (Map.Entry<String, IdentityProvider> idpEntry : authenticatorConfig.getIdps().entrySet()) {
                        IdentityProvider idp = idpEntry.getValue();
                        String idpName = idpEntry.getKey();
                        if (idp.getResourceId() == null) {
                            idPResourceId.add(getIdPByIdPName(idpName, this.tenantDomain).getResourceId());
                        } else {
                            idPResourceId.add(idp.getResourceId());
                        }
                    }
                    authenticatorConfig.setIdPResourceIds(idPResourceId);
                }
            }
        }
        authenticatedSequences.forEach((appName, sequenceConfig) -> optimizedAuthenticatedSequences.put(appName,
                new OptimizedSequenceConfig(sequenceConfig)));
        return optimizedAuthenticatedSequences;
    }

    private Map<String, OptimizedAuthenticatedIdPData> getOptimizedAuthenticatedIdPs(Map<String, AuthenticatedIdPData>
                                                                                             authenticatedIdPs) {

        Map<String, OptimizedAuthenticatedIdPData> optimizedAuthenticatedIdPs = new HashMap<>();
        authenticatedIdPs.forEach((idpName, authenticatedIdPData) -> optimizedAuthenticatedIdPs.put(idpName,
                new OptimizedAuthenticatedIdPData(authenticatedIdPData)));
        return optimizedAuthenticatedIdPs;
    }

    private Map<String, Map<String, OptimizedAuthenticatedIdPData>> getOptimizedAuthenticatedIdPsOfApp(
            Map<String, Map<String, AuthenticatedIdPData>> authenticatedIdPsOfApp) {

        Map<String, Map<String, OptimizedAuthenticatedIdPData>> optimizedAuthenticatedIdPsOfApp = new HashMap<>();
        authenticatedIdPsOfApp.forEach((appName, authenticatedIdPs) -> {
            Map<String, OptimizedAuthenticatedIdPData> optimizedAuthenticatedIdPs = new HashMap<>();
            authenticatedIdPs.forEach((idpName, authenticatedIdPData) -> optimizedAuthenticatedIdPs.put(idpName,
                    new OptimizedAuthenticatedIdPData(authenticatedIdPData)));
            optimizedAuthenticatedIdPsOfApp.put(appName, optimizedAuthenticatedIdPs);
        });
        return optimizedAuthenticatedIdPsOfApp;
    }

    public SessionContext getSessionContext() throws SessionDataStorageOptimizationException {

        if (log.isDebugEnabled()) {
            log.debug("Loading process for the session context has started.");
        }
        if (this.tenantDomain == null) {
            throw new SessionDataStorageOptimizationException("Error occurred while loading the session context");
        }
        SessionContext sessionContext = new SessionContext();
        Map<String, SequenceConfig> authenticatedSequences = new HashMap<>();
        for (Map.Entry<String, OptimizedSequenceConfig> entry : this.optimizedAuthenticatedSequences.entrySet()) {
            String appName = entry.getKey();
            OptimizedSequenceConfig optimizedSequenceConfig = entry.getValue();
            authenticatedSequences.put(appName, optimizedSequenceConfig.getSequenceConfig(this.tenantDomain));
        }
        sessionContext.setAuthenticatedSequences(authenticatedSequences);
        sessionContext.setAuthenticatedIdPs(getAuthenticatedIdPDataMap(this.optimizedAuthenticatedIdPs));
        sessionContext.setRememberMe(this.isRememberMe);
        sessionContext.setProperties(this.properties);
        sessionContext.setSessionAuthHistory(this.sessionAuthHistory);
        Map<String, Map<String, AuthenticatedIdPData>> authenticatedIdPsOfApp = new HashMap<>();
        for (Map.Entry<String, Map<String, OptimizedAuthenticatedIdPData>> entry :
                this.optimizedAuthenticatedIdPsOfApp.entrySet()) {
            String appName = entry.getKey();
            Map<String, OptimizedAuthenticatedIdPData> value = entry.getValue();
            authenticatedIdPsOfApp.put(appName, getAuthenticatedIdPDataMap(value));
        }
        sessionContext.setAuthenticatedIdPsOfApp(authenticatedIdPsOfApp);
        return sessionContext;
    }

    private Map<String, AuthenticatedIdPData> getAuthenticatedIdPDataMap(Map<String, OptimizedAuthenticatedIdPData>
                                                                                 optimizedMap)
            throws SessionDataStorageOptimizationException {

        Map<String, AuthenticatedIdPData> authenticatedIdPs = new HashMap<>();
        for (Map.Entry<String, OptimizedAuthenticatedIdPData> entry : optimizedMap.entrySet()) {
            String idpName = entry.getKey();
            OptimizedAuthenticatedIdPData optimizedAuthenticatedIdPData = entry.getValue();
            authenticatedIdPs.put(idpName, optimizedAuthenticatedIdPData.getAuthenticatedIdPData(tenantDomain));
        }
        return authenticatedIdPs;
    }

    private IdentityProvider getIdPByIdPName(String idPName, String tenantDomain)
            throws SessionDataStorageOptimizationException {

        IdentityProviderManager manager =
                (IdentityProviderManager) FrameworkServiceDataHolder.getInstance().getIdentityProviderManager();
        IdentityProvider idp;
        try {
            idp = manager.getIdPByName(idPName, tenantDomain);
            if (idp == null) {
                throw new SessionDataStorageOptimizationException(String.format(
                        "Cannot find the Identity Provider by the name: %s tenant domain: %s", idPName, tenantDomain));
            }
        } catch (IdentityProviderManagementException e) {
            throw new SessionDataStorageOptimizationException(String.format(
                    "Failed to get the Identity Provider by name: %s tenant domain: %s", idPName, tenantDomain), e);
        }
        return idp;
    }
}

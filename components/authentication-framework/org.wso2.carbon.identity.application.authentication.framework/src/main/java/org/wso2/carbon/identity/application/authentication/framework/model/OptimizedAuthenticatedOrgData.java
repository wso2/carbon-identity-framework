/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.model;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.OptimizedSequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionAuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationClientException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationException;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.storage.SessionDataStorageOptimizationServerException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
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
 * Holds the optimized authenticated data of an organization during an authentication flow.
 */
public class OptimizedAuthenticatedOrgData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, OptimizedSequenceConfig> optimizedAuthenticatedSequences;
    private final Map<String, OptimizedAuthenticatedIdPData> optimizedAuthenticatedIdPs;
    private final boolean isRememberMe;
    private final SessionAuthHistory sessionAuthHistory;
    private final Map<String, Map<String, OptimizedAuthenticatedIdPData>> optimizedAuthenticatedIdPsOfApp;

    public OptimizedAuthenticatedOrgData(AuthenticatedOrgData authenticatedOrgData)
            throws SessionDataStorageOptimizationException {

        this.optimizedAuthenticatedSequences = getOptimizedAuthenticatedSequences(
                authenticatedOrgData.getAuthenticatedSequences());
        this.optimizedAuthenticatedIdPs = getOptimizedAuthenticatedIdPs(authenticatedOrgData.getAuthenticatedIdPs());
        this.isRememberMe = authenticatedOrgData.isRememberMe();
        this.sessionAuthHistory = authenticatedOrgData.getSessionAuthHistory();
        this.optimizedAuthenticatedIdPsOfApp = getOptimizedAuthenticatedIdPsOfApp(authenticatedOrgData.
                getAuthenticatedIdPsOfApp());
    }

    private Map<String, OptimizedSequenceConfig> getOptimizedAuthenticatedSequences
            (Map<String, SequenceConfig> authenticatedSequences) throws SessionDataStorageOptimizationException {

        Map<String, OptimizedSequenceConfig> optimizedAuthenticatedSequences = new HashMap<>();
        for (Map.Entry<String, SequenceConfig> entry : authenticatedSequences.entrySet()) {
            SequenceConfig sequenceConfig = entry.getValue();
            String tenantDomain = sequenceConfig.getApplicationConfig().getServiceProvider().getTenantDomain();
            for (Map.Entry<Integer, StepConfig> mapEntry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = mapEntry.getValue();
                List<AuthenticatorConfig> authenticatorList = stepConfig.getAuthenticatorList();
                for (AuthenticatorConfig authenticatorConfig : authenticatorList) {
                    List<String> idPResourceId = new ArrayList<>();
                    for (Map.Entry<String, IdentityProvider> idpEntry : authenticatorConfig.getIdps().entrySet()) {
                        IdentityProvider idp = idpEntry.getValue();
                        String idpName = idpEntry.getKey();
                        if (StringUtils.isEmpty(idp.getResourceId())) {
                            idPResourceId.add(getIdPByIdPName(idpName, tenantDomain).getResourceId());
                        } else {
                            idPResourceId.add(idp.getResourceId());
                        }
                        if (StringUtils.isEmpty(authenticatorConfig.getTenantDomain())) {
                            authenticatorConfig.setTenantDomain(tenantDomain);
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

    private Map<String, OptimizedAuthenticatedIdPData> getOptimizedAuthenticatedIdPs(
            Map<String, AuthenticatedIdPData> authenticatedIdPs) {

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

    public AuthenticatedOrgData getAuthenticatedOrgData() throws SessionDataStorageOptimizationException {

        AuthenticatedOrgData authenticatedOrgData = new AuthenticatedOrgData();
        Map<String, SequenceConfig> authenticatedSequences = new HashMap<>();
        for (Map.Entry<String, OptimizedSequenceConfig> entry : this.optimizedAuthenticatedSequences.entrySet()) {
            String appName = entry.getKey();
            OptimizedSequenceConfig optimizedSequenceConfig = entry.getValue();
            authenticatedSequences.put(appName, optimizedSequenceConfig.getSequenceConfig());
        }
        authenticatedOrgData.setAuthenticatedSequences(authenticatedSequences);
        authenticatedOrgData.setAuthenticatedIdPs(getAuthenticatedIdPDataMap(this.optimizedAuthenticatedIdPs));
        authenticatedOrgData.setRememberMe(this.isRememberMe);
        authenticatedOrgData.setSessionAuthHistory(this.sessionAuthHistory);
        Map<String, Map<String, AuthenticatedIdPData>> authenticatedIdPsOfApp = new HashMap<>();
        for (Map.Entry<String, Map<String, OptimizedAuthenticatedIdPData>> entry :
                this.optimizedAuthenticatedIdPsOfApp.entrySet()) {
            String appName = entry.getKey();
            Map<String, OptimizedAuthenticatedIdPData> value = entry.getValue();
            authenticatedIdPsOfApp.put(appName, getAuthenticatedIdPDataMap(value));
        }
        authenticatedOrgData.setAuthenticatedIdPsOfApp(authenticatedIdPsOfApp);
        return authenticatedOrgData;
    }

    private Map<String, AuthenticatedIdPData> getAuthenticatedIdPDataMap(
            Map<String, OptimizedAuthenticatedIdPData> optimizedMap)
            throws SessionDataStorageOptimizationException {

        Map<String, AuthenticatedIdPData> authenticatedIdPs = new HashMap<>();
        for (Map.Entry<String, OptimizedAuthenticatedIdPData> entry : optimizedMap.entrySet()) {
            String idpName = entry.getKey();
            OptimizedAuthenticatedIdPData optimizedAuthenticatedIdPData = entry.getValue();
            authenticatedIdPs.put(idpName, optimizedAuthenticatedIdPData.getAuthenticatedIdPData());
        }
        return authenticatedIdPs;
    }

    private IdentityProvider getIdPByIdPName(String idPName, String tenantDomain)
            throws SessionDataStorageOptimizationException {

        if (StringUtils.isEmpty(tenantDomain)) {
            throw new SessionDataStorageOptimizationClientException("Tenant domain is null. " +
                    " Error occurred while getting idp by name: " + idPName);
        }
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
                    "IDP management client error while retrieving the Identity Provider by name: %s " +
                            "tenant domain: %s", idPName, tenantDomain), e);
        } catch (IdentityProviderManagementServerException e) {
            throw new SessionDataStorageOptimizationServerException(String.format(
                    "IDP management server error while retrieving the Identity Provider by name: %s " +
                            "tenant domain: %s", idPName, tenantDomain), e);
        } catch (IdentityProviderManagementException e) {
            throw new SessionDataStorageOptimizationServerException(String.format(
                    "Error while retrieving the Identity Provider by name: %s tenant domain: %s", idPName,
                    tenantDomain), e);
        }
        return idp;
    }
}

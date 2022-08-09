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

package org.wso2.carbon.identity.application.authentication.framework.config.model;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.AuthenticationGraph;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionContextLoaderException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is class is used to store the optimized sequence config attributes in session context.
 */
public class OptimizedSequenceConfig implements Serializable {

    private String name;
    private boolean isForceAuthn;
    private boolean isCheckAuthn;
    private String applicationId;
    private Map<Integer, OptimizedStepConfig> optimizedStepMap;
    private AuthenticationGraph authenticationGraph;
    private List<AuthenticatorConfig> reqPathAuthenticators;
    private String applicationResourceId;
    private boolean completed;

    private AuthenticatedUser authenticatedUser;
    private String authenticatedIdPs;

    private AuthenticatorConfig authenticatedReqPathAuthenticator;
    private List<String> requestedAcr;

    public OptimizedSequenceConfig(SequenceConfig sequenceConfig) {

        this.name = sequenceConfig.getName();
        this.isForceAuthn = sequenceConfig.isForceAuthn();
        this.isCheckAuthn = sequenceConfig.isCheckAuthn();
        this.applicationId = sequenceConfig.getApplicationId();
        this.optimizedStepMap = getOptimizedStepMap(sequenceConfig.getStepMap());
        this.authenticationGraph = sequenceConfig.getAuthenticationGraph();
        this.reqPathAuthenticators = sequenceConfig.getReqPathAuthenticators();
        if (sequenceConfig.getApplicationConfig() != null) {
            this.applicationResourceId = sequenceConfig.getApplicationConfig().
                    getServiceProvider().getApplicationResourceId();
        }
        this.completed = sequenceConfig.isCompleted();
        this.authenticatedUser = sequenceConfig.getAuthenticatedUser();
        this.authenticatedIdPs = sequenceConfig.getAuthenticatedIdPs();
        this.authenticatedReqPathAuthenticator = sequenceConfig.getAuthenticatedReqPathAuthenticator();
        this.requestedAcr = sequenceConfig.getRequestedAcr();

    }

    private Map<Integer, OptimizedStepConfig> getOptimizedStepMap(Map<Integer, StepConfig> stepMap) {

        Map<Integer, OptimizedStepConfig> optimizedStepMap = new HashMap<>();
        stepMap.forEach((order, stepConfig) -> {
            optimizedStepMap.put(order, new OptimizedStepConfig(stepConfig));
        });
        return optimizedStepMap;
    }

    public SequenceConfig getSequenceConfig(String tenantDomain) throws SessionContextLoaderException {

        SequenceConfig sequenceConfig = new SequenceConfig();
        sequenceConfig.setName(this.name);
        sequenceConfig.setForceAuthn(this.isForceAuthn);
        sequenceConfig.setCheckAuthn(this.isCheckAuthn);
        sequenceConfig.setApplicationId(this.applicationId);
        Map<Integer, StepConfig> stepMap = new HashMap<>();
        for (Map.Entry<Integer, OptimizedStepConfig> entry : optimizedStepMap.entrySet()) {
            Integer order = entry.getKey();
            OptimizedStepConfig optimizedStepConfig = entry.getValue();
            StepConfig stepConfig = optimizedStepConfig.getStepConfig(tenantDomain);
            stepMap.put(order, stepConfig);
        }
        sequenceConfig.setStepMap(stepMap);
        sequenceConfig.setAuthenticationGraph(this.authenticationGraph);
        sequenceConfig.setReqPathAuthenticators(this.reqPathAuthenticators);
        sequenceConfig.setApplicationConfig(getApplicationConfig(this.applicationResourceId, tenantDomain));
        sequenceConfig.setCompleted(this.completed);
        sequenceConfig.setAuthenticatedUser(this.authenticatedUser);
        sequenceConfig.setAuthenticatedIdPs(this.authenticatedIdPs);
        sequenceConfig.setAuthenticatedReqPathAuthenticator(this.authenticatedReqPathAuthenticator);
        sequenceConfig.setRequestedAcr(this.requestedAcr);
        return sequenceConfig;
    }

    private ApplicationConfig getApplicationConfig(String resourceId, String tenantDomain) throws
            SessionContextLoaderException {

        if (resourceId == null) {
            throw new SessionContextLoaderException("Error occurred while getting Service Provider");
        }
        ServiceProvider serviceProvider;
        ApplicationManagementServiceImpl applicationManager = (ApplicationManagementServiceImpl)
                FrameworkServiceDataHolder.getInstance().getApplicationManagementService();
        try {
            serviceProvider = applicationManager.getApplicationByResourceId(
                    this.applicationResourceId, tenantDomain);
            if (serviceProvider == null) {
                throw new SessionContextLoaderException(
                        String.format("Cannot find the Service Provider by the resource ID: %s " +
                                "tenant domain: %s", resourceId, tenantDomain));
            }
        } catch (IdentityApplicationManagementException e) {
            throw new SessionContextLoaderException(
                    String.format("Error occurred while retrieving the service provider by resource id: %s " +
                            "tenant domain: %s", this.applicationResourceId, tenantDomain), e);
        }
        return new ApplicationConfig(serviceProvider, tenantDomain);
    }
}

/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.internal;

import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.flow.execution.engine.graph.Executor;
import org.wso2.carbon.identity.flow.execution.engine.listener.FlowExecutionListener;
import org.wso2.carbon.identity.flow.mgt.FlowMgtService;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data holder for the User Flow Service.
 */
public class FlowExecutionEngineDataHolder {

    private static final Map<String, Executor> executors = new HashMap<>();
    private static final FlowExecutionEngineDataHolder instance = new FlowExecutionEngineDataHolder();
    private FlowMgtService flowMgtService;
    private RealmService realmService;
    private InputValidationManagementService inputValidationManagementService;
    private ApplicationManagementService applicationManagementService;
    private FederatedAssociationManager federatedAssociationManager;
    private List<FlowExecutionListener> flowExecutionListeners = new ArrayList<>();

    private FlowExecutionEngineDataHolder() {

    }

    public static FlowExecutionEngineDataHolder getInstance() {

        return instance;
    }

    /**
     * Add an executor to the executors map.
     *
     * @return Registered map of executors.
     */
    public Map<String, Executor> getExecutors() {

        return executors;
    }

    /**
     * Get the realm service.
     *
     * @return Realm service.
     */
    public RealmService getRealmService() {

        return realmService;
    }

    /**
     * Set the realm service.
     *
     * @param realmService Realm service.
     */
    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Get the flow management service.
     *
     * @return Flow management service.
     */
    public FlowMgtService getFlowMgtService() {

        return flowMgtService;
    }

    /**
     * Set the flow management service.
     *
     * @param flowMgtService Flow management service.
     */
    public void setFlowMgtService(FlowMgtService flowMgtService) {

        this.flowMgtService = flowMgtService;
    }

    /**
     * Get the input validation management service.
     *
     * @return Input validation management service.
     */
    public InputValidationManagementService getInputValidationManagementService() {

        return inputValidationManagementService;
    }

    /**
     * Set the input validation management service.
     *
     * @param inputValidationManagementService Input validation management service.
     */
    public void setInputValidationManagementService(InputValidationManagementService inputValidationManagementService) {

        this.inputValidationManagementService = inputValidationManagementService;
    }

    /**
     * Get the list of flow execution listeners.
     *
     * @return List of flow execution listeners.
     */
    public List<FlowExecutionListener> getFlowListeners() {

        return flowExecutionListeners;
    }

    /**
     * Set the list of flow execution listeners.
     *
     * @param flowExecutionListeners List of flow execution listeners.
     */
    public void addFlowExecutionListeners(FlowExecutionListener flowExecutionListeners) {

        this.flowExecutionListeners.add(flowExecutionListeners);
    }

    /**
     * Get the application management service.
     *
     * @return Application management service.
     */
    public ApplicationManagementService getApplicationManagementService() {

        return applicationManagementService;
    }

    /**
     * Set the application management service.
     *
     * @param applicationManagementService Application management service.
     */
    public void setApplicationManagementService(
            ApplicationManagementService applicationManagementService) {

        this.applicationManagementService = applicationManagementService;
    }

    /**
     * Get the federated association manager.
     *
     * @return Federated association manager.
     */
    public FederatedAssociationManager getFederatedAssociationManager() {

        return federatedAssociationManager;
    }

    /**
     * Set the federated association manager.
     *
     * @param federatedAssociationManager Federated association manager.
     */
    public void setFederatedAssociationManager(FederatedAssociationManager federatedAssociationManager) {

        this.federatedAssociationManager = federatedAssociationManager;
    }
}

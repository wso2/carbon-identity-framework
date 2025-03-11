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

package org.wso2.carbon.identity.user.registration.engine.internal;

import java.util.HashMap;
import java.util.Map;

import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementServiceImpl;
import org.wso2.carbon.identity.input.validation.mgt.services.InputValidationManagementService;
import org.wso2.carbon.identity.user.registration.engine.graph.Executor;
import org.wso2.carbon.identity.user.registration.mgt.RegistrationFlowMgtService;
import org.wso2.carbon.identity.user.registration.engine.graph.Node;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for the User Registration Service.
 */
public class RegistrationFlowEngineDataHolder {

    private static final Map<String, Executor> executors = new HashMap<>();
    private RegistrationFlowMgtService registrationFlowMgtService;
    private RealmService realmService;
    private InputValidationManagementService inputValidationManagementService;
    private static final RegistrationFlowEngineDataHolder instance = new RegistrationFlowEngineDataHolder();

    private RegistrationFlowEngineDataHolder() {

    }

    public static RegistrationFlowEngineDataHolder getInstance() {

        return instance;
    }

    /**
     * Add an executor to the executors map.
     *
     * @return  Registered map of executors.
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
     * @param realmService  Realm service.
     */
    public void setRealmService(RealmService realmService) {

        this.realmService = realmService;
    }

    /**
     * Get the registration flow management service.
     *
     * @return  Registration flow management service.
     */
    public RegistrationFlowMgtService getRegistrationFlowMgtService() {

        return registrationFlowMgtService;
    }

    /**
     * Set the registration flow management service.
     *
     * @param registrationFlowMgtService    Registration flow management service.
     */
    public void setRegistrationFlowMgtService(RegistrationFlowMgtService registrationFlowMgtService) {

        this.registrationFlowMgtService = registrationFlowMgtService;
    }

    /**
     * Get the input validation management service.
     *
     * @return  Input validation management service.
     */
    public InputValidationManagementService getInputValidationManagementService() {

        return inputValidationManagementService;
    }

    /**
     * Set the input validation management service.
     *
     * @param inputValidationManagementService    Input validation management service.
     */
    public void setInputValidationManagementService(InputValidationManagementService inputValidationManagementService) {

        this.inputValidationManagementService = inputValidationManagementService;
    }
}

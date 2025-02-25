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

import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.user.registration.engine.executor.Executor;
import org.wso2.carbon.identity.user.registration.mgt.RegistrationFlowMgtService;
import org.wso2.carbon.identity.user.registration.engine.node.Node;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.List;

/**
 * Data holder for the User Registration Service.
 */
public class RegistrationFlowEngineDataHolder {

    private static final List<Executor> executors = new ArrayList<>();
    private static final List<Node> nodes = new ArrayList<>();
    private RegistrationFlowMgtService registrationFlowMgtService;
    private RealmService realmService;
    private static final RegistrationFlowEngineDataHolder instance = new RegistrationFlowEngineDataHolder();

    private RegistrationFlowEngineDataHolder() {

    }

    public static RegistrationFlowEngineDataHolder getInstance() {

        return instance;
    }

    /**
     * Get the list of registered executors.
     *
     * @return List of executors.
     */
    public List<Executor> getExecutors() {

        return executors;
    }

    /**
     * Get the list of registered nodes.
     *
     * @return List of nodes.
     */
    public List<Node> getNodes() {

        return nodes;
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
}

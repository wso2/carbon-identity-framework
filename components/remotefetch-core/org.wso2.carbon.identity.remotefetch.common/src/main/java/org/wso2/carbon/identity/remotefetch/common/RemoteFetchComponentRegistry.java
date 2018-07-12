/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.remotefetch.common;

import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerComponent;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerComponent;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerComponent;

/**
 * Interface for registry that allows to register different components
 */
public interface RemoteFetchComponentRegistry {

    /**
     *
     * @param repositoryManagerComponent
     */
    void registerRepositoryManager(RepositoryManagerComponent repositoryManagerComponent);

    /**
     *
     * @param configDeployerComponent
     */
    void registerConfigDeployer(ConfigDeployerComponent configDeployerComponent);

    /**
     *
     * @param actionListenerComponent
     */
    void registerActionListener(ActionListenerComponent actionListenerComponent);

    /**
     *
     * @param identifier
     */
    void deRegisterRepositoryManager(String identifier);

    /**
     *
     * @param identifier
     */
    void deRegisterConfigDeployer(String identifier);

    /**
     *
     * @param identifier
     */
    void deRegisterActionListener(String identifier);

    /**
     *
     * @param identifier
     * @return
     */
    RepositoryManagerComponent getRepositoryManagerComponent(String identifier);

    /**
     *
     * @param identifier
     * @return
     */
    ConfigDeployerComponent getConfigDeployerComponent(String identifier);

    /**
     *
     * @param identifier
     * @return
     */
    ActionListenerComponent getActionListenerComponent(String identifier);
}

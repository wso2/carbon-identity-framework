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

package org.wso2.carbon.identity.remotefetch.core;

import org.wso2.carbon.identity.remotefetch.common.RemoteFetchComponentRegistry;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerComponent;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerComponent;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerComponent;

import java.util.HashMap;

public class RemoteFetchComponentRegistryImpl implements RemoteFetchComponentRegistry {

    private HashMap<String,RepositoryManagerComponent> repositoryManagerComponentMap = new HashMap<>();
    private HashMap<String,ConfigDeployerComponent> configDeployerComponentMap = new HashMap<>();
    private HashMap<String,ActionListenerComponent> actionListenerComponentMap = new HashMap<>();

    /**
     * @param repositoryManagerComponent
     */
    @Override
    public void registerRepositoryManager(RepositoryManagerComponent repositoryManagerComponent) {
        this.repositoryManagerComponentMap.put(repositoryManagerComponent.getType(),repositoryManagerComponent);
    }

    /**
     * @param configDeployerComponent
     */
    @Override
    public void registerConfigDeployer(ConfigDeployerComponent configDeployerComponent) {
        this.configDeployerComponentMap.put(configDeployerComponent.getType(),configDeployerComponent);
    }

    /**
     * @param actionListenerComponent
     */
    @Override
    public void registerActionListener(ActionListenerComponent actionListenerComponent) {
        this.actionListenerComponentMap.put(actionListenerComponent.getType(),actionListenerComponent);
    }

    /**
     * @param identifier
     */
    @Override
    public void deRegisterRepositoryManager(String identifier) {
        this.repositoryManagerComponentMap.remove(identifier);
    }

    /**
     * @param identifier
     */
    @Override
    public void deRegisterConfigDeployer(String identifier) {
        this.configDeployerComponentMap.remove(identifier);
    }

    /**
     * @param identifier
     */
    @Override
    public void deRegisterActionListener(String identifier) {
        this.actionListenerComponentMap.remove(identifier);
    }

    /**
     * @param identifier
     * @return
     */
    @Override
    public RepositoryManagerComponent getRepositoryManagerComponent(String identifier) {

        return this.repositoryManagerComponentMap.getOrDefault(identifier,null);
    }

    /**
     * @param identifier
     * @return
     */
    @Override
    public ConfigDeployerComponent getConfigDeployerComponent(String identifier) {

        return this.configDeployerComponentMap.getOrDefault(identifier,null);
    }

    /**
     * @param identifier
     * @return
     */
    @Override
    public ActionListenerComponent getActionListenerComponent(String identifier) {

        return this.actionListenerComponentMap.getOrDefault(identifier,null);
    }
}

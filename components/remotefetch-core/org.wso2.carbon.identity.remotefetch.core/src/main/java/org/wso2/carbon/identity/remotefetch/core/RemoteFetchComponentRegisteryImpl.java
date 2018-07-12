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

import org.wso2.carbon.identity.remotefetch.common.RemoteFetchComponentRegistery;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerComponent;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerComponent;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerComponent;

import java.util.HashMap;
import java.util.Map;

public class RemoteFetchComponentRegisteryImpl implements RemoteFetchComponentRegistery {

    private HashMap<String,RepositoryManagerComponent> repositoryManagerConnectorMap = new HashMap<>();
    private HashMap<String,ConfigDeployerComponent> configDeployerConnectorMap = new HashMap<>();
    private HashMap<String,ActionListenerComponent> actionListenerConnectorMap = new HashMap<>();

    @Override
    public void registerRepositoryManager(RepositoryManagerComponent repositoryManagerComponent) {
        this.repositoryManagerConnectorMap.put(repositoryManagerComponent.getType(), repositoryManagerComponent);
    }

    @Override
    public void registerConfigDeployer(ConfigDeployerComponent configDeployerComponent) {
        this.configDeployerConnectorMap.put(configDeployerComponent.getType(), configDeployerComponent);
    }

    @Override
    public void registerActionListener(ActionListenerComponent actionListenerComponent) {
        this.actionListenerConnectorMap.put(actionListenerComponent.getType(), actionListenerComponent);
    }

    @Override
    public void deRegisterRepositoryManager(String identifier) {
        this.repositoryManagerConnectorMap.remove(identifier);
    }

    @Override
    public void deRegisterConfigDeployer(String identifier) {
        this.configDeployerConnectorMap.remove(identifier);
    }

    @Override
    public void deRegisterActionListener(String identifier) {
        this.actionListenerConnectorMap.remove(identifier);
    }

    @Override
    public Map<String, RepositoryManagerComponent> getRepositoryManagerConnectorMap() {

        return this.repositoryManagerConnectorMap;
    }

    @Override
    public Map<String, ConfigDeployerComponent> getConfigDeployerConnectorMap() {

        return this.configDeployerConnectorMap;
    }

    @Override
    public Map<String, ActionListenerComponent> getActionListenerConnectorMap() {

        return this.actionListenerConnectorMap;
    }
}

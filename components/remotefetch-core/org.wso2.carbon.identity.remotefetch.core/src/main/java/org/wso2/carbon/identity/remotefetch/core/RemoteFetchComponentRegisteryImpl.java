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
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerConnector;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerConnector;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerConnector;

import java.util.HashMap;
import java.util.Map;

public class RemoteFetchComponentRegisteryImpl implements RemoteFetchComponentRegistery {

    private HashMap<String,RepositoryManagerConnector> repositoryManagerConnectorMap = new HashMap<>();
    private HashMap<String,ConfigDeployerConnector> configDeployerConnectorMap = new HashMap<>();
    private HashMap<String,ActionListenerConnector> actionListenerConnectorMap = new HashMap<>();

    @Override
    public void registerRepositoryManager(RepositoryManagerConnector repositoryManagerConnector) {
        this.repositoryManagerConnectorMap.put(repositoryManagerConnector.getType(),repositoryManagerConnector);
    }

    @Override
    public void registerConfigDeployer(ConfigDeployerConnector configDeployerConnector) {
        this.configDeployerConnectorMap.put(configDeployerConnector.getType(),configDeployerConnector);
    }

    @Override
    public void registerActionListener(ActionListenerConnector actionListenerConnector) {
        this.actionListenerConnectorMap.put(actionListenerConnector.getType(),actionListenerConnector);
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
    public Map<String, RepositoryManagerConnector> getRepositoryManagerConnectorMap() {

        return this.repositoryManagerConnectorMap;
    }

    @Override
    public Map<String, ConfigDeployerConnector> getConfigDeployerConnectorMap() {

        return this.configDeployerConnectorMap;
    }

    @Override
    public Map<String, ActionListenerConnector> getActionListenerConnectorMap() {

        return this.actionListenerConnectorMap;
    }
}

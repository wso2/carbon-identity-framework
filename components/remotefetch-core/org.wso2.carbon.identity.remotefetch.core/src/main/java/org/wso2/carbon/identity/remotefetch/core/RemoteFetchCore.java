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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.remotefetch.common.RemoteFetchComponentRegistry;
import org.wso2.carbon.identity.remotefetch.common.RemoteFetchConfiguration;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListener;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerBuilder;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerBuilderException;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployer;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerBuilder;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerBuilderException;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManager;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerBuilder;
import org.wso2.carbon.identity.remotefetch.common.repomanager.RepositoryManagerBuilderException;
import org.wso2.carbon.identity.remotefetch.core.dao.RemoteFetchConfigurationDAO;
import org.wso2.carbon.identity.remotefetch.core.dao.impl.RemoteFetchConfigurationDAOImpl;
import org.wso2.carbon.identity.remotefetch.core.internal.RemoteFetchServiceComponentHolder;

import java.util.ArrayList;
import java.util.List;

public class RemoteFetchCore implements Runnable{

    private Log log = LogFactory.getLog(RemoteFetchCore.class);
    private RemoteFetchConfigurationDAO remoteFetchConfigDAO;
    private List<ActionListener> listenersList = new ArrayList<>();
    private RemoteFetchComponentRegistry componentRegistry;

    public RemoteFetchCore(){
        this.remoteFetchConfigDAO = new RemoteFetchConfigurationDAOImpl();
        this.componentRegistry = RemoteFetchServiceComponentHolder.getInstance().getRemoteFetchComponentRegistry();
    }

    private ActionListener buildListener(RemoteFetchConfiguration fetchConfig) throws RemoteFetchCoreException{

        RepositoryManager repoConnector;
        ActionListener actionListener;
        ConfigDeployer configDeployer;

        try {
            RepositoryManagerBuilder repositoryManagerBuilder = this.componentRegistry
                    .getRepositoryManagerComponent(fetchConfig.getRepositoryManagerType())
                    .getRepositoryManagerBuilder();

            repoConnector = repositoryManagerBuilder.addRemoteFetchConfig(fetchConfig).build();
        } catch (NullPointerException e) {
            throw new RemoteFetchCoreException("Unable to retrieve specified RepositoryManager", e);
        } catch (RepositoryManagerBuilderException e){
            throw new RemoteFetchCoreException("Unable to build RepositoryManager object", e);
        }

        try {
            ConfigDeployerBuilder configDeployerBuilder = this.componentRegistry
                    .getConfigDeployerComponent(fetchConfig.getConfgiurationDeployerType())
                    .getConfigDeployerBuilder();

            configDeployer = configDeployerBuilder.addRemoteFetchConfig(fetchConfig).build();

        } catch (NullPointerException e) {
            throw new RemoteFetchCoreException("Unable to retrieve specified ConfigDeployer", e);
        } catch (ConfigDeployerBuilderException e){
            throw new RemoteFetchCoreException("Unable to build ConfigDeployer object", e);
        }

        try {
            ActionListenerBuilder actionListenerBuilder = this.componentRegistry
                    .getActionListenerComponent(fetchConfig.getActionListenerType())
                    .getActionListenerBuilder();

            actionListener = actionListenerBuilder.addRemoteFetchConfig(fetchConfig)
                    .addConfigDeployer(configDeployer).addRepositoryConnector(repoConnector).build();

        } catch (NullPointerException e) {
            throw new RemoteFetchCoreException("Unable to retrieve specified ActionListener", e);
        } catch (ActionListenerBuilderException e){
            throw new RemoteFetchCoreException("Unable to build ActionListener object", e);
        }

        return actionListener;

    }

    private void seedListeners(){
        try {
            this.remoteFetchConfigDAO.getAllRemoteFetchConfigurations().forEach((RemoteFetchConfiguration config) ->{
                try {
                    this.listenersList.add(this.buildListener(config));
                } catch (RemoteFetchCoreException e){
                    log.error("Exception building ActionListener",e);
                }
            });
        } catch (RemoteFetchCoreException e) {
            log.error("Unable to list RemoteFetchConfigurations", e);
        }
    }

    @Override
    public void run() {
        if (listenersList.isEmpty()) seedListeners();
        for (ActionListener actionListener: listenersList) {
            actionListener.iteration();
        }
    }
}

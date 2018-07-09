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
import org.wso2.carbon.identity.remotefetch.common.RemoteFetchConfiguration;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListener;
import org.wso2.carbon.identity.remotefetch.common.actionlistener.ActionListenerBuilderException;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployer;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerBuilder;
import org.wso2.carbon.identity.remotefetch.common.configdeployer.ConfigDeployerBuilderException;
import org.wso2.carbon.identity.remotefetch.common.exceptions.RemoteFetchCoreException;
import org.wso2.carbon.identity.remotefetch.common.repoconnector.RepositoryConnector;
import org.wso2.carbon.identity.remotefetch.common.repoconnector.RepositoryConnectorBuilder;
import org.wso2.carbon.identity.remotefetch.common.repoconnector.RepositoryConnectorBuilderException;
import org.wso2.carbon.identity.remotefetch.core.dao.RemoteFetchConfigurationDAO;
import org.wso2.carbon.identity.remotefetch.core.dao.impl.RemoteFetchConfigurationDAOImpl;
import org.wso2.carbon.identity.remotefetch.core.implementations.actionHandlers.PollingActionListenerBuilder;
import org.wso2.carbon.identity.remotefetch.core.implementations.configDeployers.SoutConfigDeployerBuilder;
import org.wso2.carbon.identity.remotefetch.core.implementations.repositoryHandlers.GitRepositoryConnectorBuilder;

import java.util.ArrayList;
import java.util.List;

public class RemoteFetchCore implements Runnable{

    private Log log = LogFactory.getLog(RemoteFetchCore.class);
    private RemoteFetchConfigurationDAO remoteFetchConfigDAO;
    private List<ActionListener> listenersList = new ArrayList<>();

    public RemoteFetchCore(){
        this.remoteFetchConfigDAO = new RemoteFetchConfigurationDAOImpl();
    }

    private RepositoryConnectorBuilder getRepositoryConnectorBuilder(RemoteFetchConfiguration fetchConfig)
            throws RemoteFetchCoreException{
        switch (fetchConfig.getRepositoryConnectorType()){
            case "git":
                return new GitRepositoryConnectorBuilder();
            default:
                throw new RemoteFetchCoreException("No such registered RepositoryConnector");
        }
    }
    private PollingActionListenerBuilder getPollingActionListenerBuilder(RemoteFetchConfiguration fetchConfig)
            throws RemoteFetchCoreException{
        switch (fetchConfig.getActionListenerType()){
            case "polling":
                return new PollingActionListenerBuilder();
            default:
                throw new RemoteFetchCoreException("No such registered ActionListener");
        }
    }

    private ConfigDeployerBuilder getConfigDeployerBuilder(RemoteFetchConfiguration fetchConfig)
            throws RemoteFetchCoreException{
        switch (fetchConfig.getConfgiurationDeployerType()){
            case "Sout":
                return new SoutConfigDeployerBuilder();
            default:
                throw new RemoteFetchCoreException("No such registered ConfigDeployer");
        }
    }

    private ActionListener buildListener(RemoteFetchConfiguration fetchConfig) throws Exception{

        RepositoryConnector repoConnector;
        ActionListener actionListener;
        ConfigDeployer configDeployer;

        try {
            repoConnector = getRepositoryConnectorBuilder(fetchConfig).addRemoteFetchConfig(fetchConfig).build();
        } catch (RemoteFetchCoreException e) {
            throw e;
        } catch (RepositoryConnectorBuilderException e){
            throw e;
        }

        try {
            configDeployer = getConfigDeployerBuilder(fetchConfig).addRemoteFetchConfig(fetchConfig).build();
        } catch (RemoteFetchCoreException e) {
            throw e;
        } catch (ConfigDeployerBuilderException e){
            throw e;
        }

        try {
            actionListener = getPollingActionListenerBuilder(fetchConfig).addRemoteFetchConfig(fetchConfig)
                    .addConfigDeployer(configDeployer).addRepositoryConnector(repoConnector).build();
        } catch (RemoteFetchCoreException e) {
            throw e;
        } catch (ActionListenerBuilderException e){
            throw e;
        }
        return actionListener;

    }

    private void seedListeners(){
        try {
            this.remoteFetchConfigDAO.getAllRemoteFetchConfigurations().forEach((RemoteFetchConfiguration config) ->{
                try {
                    this.listenersList.add(this.buildListener(config));
                } catch (Exception e){
                    log.info("Exception when building config",e);
                }
            });
        } catch (RemoteFetchCoreException e) {
            log.info("Unable to read configurations", e);
        }
    }

    @Override
    public void run() {
        if (listenersList.isEmpty()) seedListeners();
        this.listenersList.forEach((ActionListener actionListener) -> {
            actionListener.iteration();
        });
    }
}

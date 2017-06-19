/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.identity.application.common.internal;

import org.apache.axis2.clustering.ClusteringAgent;
import org.wso2.carbon.base.api.ServerConfigurationService;

public class DataHolder {

    private static final DataHolder instance = new DataHolder();

    private ClusteringAgent clusteringAgent;
    private ServerConfigurationService serverConfigurationService;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return instance;
    }

    public ClusteringAgent getClusteringAgent() {
        return clusteringAgent;
    }

    public void setClusteringAgent(ClusteringAgent clusteringAgent) {
        this.clusteringAgent = clusteringAgent;
    }

    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        this.serverConfigurationService = serverConfigurationService;
    }

}

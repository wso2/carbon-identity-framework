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

import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="identity.application.common.dscomponent"
 * @scr.reference name="config.context.service1"
 *                interface="org.wso2.carbon.utils.ConfigurationContextService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setConfigurationContextService"
 *                unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration"
 *                interface="org.wso2.carbon.base.api.ServerConfigurationService"
 *                cardinality="1..1" policy="dynamic"
 *                bind="setServerConfiguration"
 *                unbind="unsetServerConfiguration"
 */
public class ApplicationCommonServiceComponent {

    private static ServerConfigurationService serverConfiguration;

    protected void setServerConfiguration(ServerConfigurationService configuration) {
        DataHolder.getInstance().setServerConfigurationService(configuration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService configuration) {
        DataHolder.getInstance().setServerConfigurationService(null);
    }

    public static ServerConfigurationService getServerConfiguration() throws Exception {
        if (serverConfiguration == null) {
            String msg = "Server configuration is null. Some bundles in the system have not started";
            throw new Exception(msg);
        }
        return serverConfiguration;
    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setClusteringAgent(contextService.getServerConfigContext().getAxisConfiguration()
                .getClusteringAgent());
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        DataHolder.getInstance().setClusteringAgent(null);
    }
}

/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common;

import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Provisioning connector service.
 */
public class ProvisioningConnectorService {

    private static volatile ProvisioningConnectorService instance;

    private List<ProvisioningConnectorConfig> provisioningConnectorConfigs = new ArrayList<>();

    public static ProvisioningConnectorService getInstance() {

        if (instance == null) {
            synchronized (ProvisioningConnectorService.class) {
                if (instance == null) {
                    instance = new ProvisioningConnectorService();
                }
            }
        }
        return instance;
    }

    public List<ProvisioningConnectorConfig> getProvisioningConnectorConfigs() {

        return provisioningConnectorConfigs;
    }

    public void addProvisioningConnectorConfigs(ProvisioningConnectorConfig connectorConfig) {

        if (connectorConfig != null) {
            provisioningConnectorConfigs.add(connectorConfig);
        }
    }

    public void removeProvisioningConnectorConfigs(ProvisioningConnectorConfig connectorConfig) {

        if (connectorConfig != null) {
            provisioningConnectorConfigs.remove(connectorConfig);
        }
    }

    public ProvisioningConnectorConfig getProvisioningConnectorByName(String name) {

        for (ProvisioningConnectorConfig connectorConfig : provisioningConnectorConfigs) {
            if (connectorConfig.getName().equals(name)) {
                return connectorConfig;
            }
        }
        return null;
    }

}

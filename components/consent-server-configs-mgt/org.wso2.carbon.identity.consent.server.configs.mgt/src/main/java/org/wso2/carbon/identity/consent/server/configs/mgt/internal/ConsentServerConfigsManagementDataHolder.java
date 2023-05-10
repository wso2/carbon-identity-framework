/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.consent.server.configs.mgt.internal;

import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;

/**
 * Holds data for External Consent Management Server Configs.
 */
public class ConsentServerConfigsManagementDataHolder {

    private static ConsentServerConfigsManagementDataHolder instance = new ConsentServerConfigsManagementDataHolder();
    private static ConfigurationManager configurationManager = null;

    /**
     * Get Configuration Manager.
     *
     * @return ConfigurationManager.
     */
    public static ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    /**
     * Set Configuration manager.
     *
     * @param configurationManager Configuration Manager.
     */
    public static void setConfigurationManager(ConfigurationManager configurationManager) {

        ConsentServerConfigsManagementDataHolder.configurationManager = configurationManager;
    }

}

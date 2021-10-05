/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.cors.mgt.core.util;

import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManagerImpl;
import org.wso2.carbon.identity.configuration.mgt.core.dao.ConfigurationDAO;
import org.wso2.carbon.identity.configuration.mgt.core.dao.impl.ConfigurationDAOImpl;
import org.wso2.carbon.identity.configuration.mgt.core.internal.ConfigurationManagerComponentDataHolder;
import org.wso2.carbon.identity.configuration.mgt.core.model.ConfigurationManagerConfigurationHolder;

import java.util.Collections;

/**
 * Utility class for Configuration Management functions.
 */
public class ConfigurationManagementUtils {

    /**
     * Private constructor of ConfigurationManagementUtils.
     */
    private ConfigurationManagementUtils() {

    }

    public static ConfigurationManager getConfigurationManager() {

        ConfigurationManagerComponentDataHolder.setUseCreatedTime(true);
        ConfigurationManagerConfigurationHolder configurationHolder = new ConfigurationManagerConfigurationHolder();

        ConfigurationDAO configurationDAO = new ConfigurationDAOImpl();
        configurationHolder.setConfigurationDAOS(Collections.singletonList(configurationDAO));

        ConfigurationManager configurationManager = new ConfigurationManagerImpl(configurationHolder);
        ConfigurationManagerComponentDataHolder.getInstance().setConfigurationManagementEnabled(true);

        return configurationManager;
    }
}

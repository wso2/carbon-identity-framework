/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.configuration.mgt.core.internal;

/**
 * A class to keep the data of the configuration manager component.
 */
public class ConfigurationManagerComponentDataHolder {

    private static ConfigurationManagerComponentDataHolder instance = new ConfigurationManagerComponentDataHolder();
    private static boolean useCreatedTime = false;

    private boolean configurationManagementEnabled;

    public static ConfigurationManagerComponentDataHolder getInstance() {

        return instance;
    }

    public static boolean getUseCreatedTime() {

        return ConfigurationManagerComponentDataHolder.useCreatedTime;
    }

    public static void setUseCreatedTime(boolean useCreatedTime) {

        ConfigurationManagerComponentDataHolder.useCreatedTime = useCreatedTime;
    }

    public boolean isConfigurationManagementEnabled() {

        return configurationManagementEnabled;
    }

    public void setConfigurationManagementEnabled(boolean configurationManagementEnabled) {

        this.configurationManagementEnabled = configurationManagementEnabled;
    }
}

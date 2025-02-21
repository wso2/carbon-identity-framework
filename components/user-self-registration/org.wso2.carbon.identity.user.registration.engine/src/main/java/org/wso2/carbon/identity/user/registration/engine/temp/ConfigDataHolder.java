/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.engine.temp;

import java.util.HashMap;
import java.util.Map;

/**
 * Data holder for the configuration data.
 * Holds the configuration data for the registration flow.
 */
public class ConfigDataHolder {

    private static volatile ConfigDataHolder instance = new ConfigDataHolder();
    private Map<String, String> orchestrationConfig = new HashMap<>();

    private ConfigDataHolder() {

    }

    public static ConfigDataHolder getInstance() {

        return instance;
    }

    public Map<String, String> getOrchestrationConfig() {

        return orchestrationConfig;
    }
}

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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.cors.mgt.core.internal;

import org.wso2.carbon.identity.configuration.mgt.core.ConfigurationManager;
import org.wso2.carbon.identity.cors.mgt.core.CORSManagementService;

/**
 * Service holder class for CORS-Service.
 */
public class CORSManagementServiceHolder {

    private ConfigurationManager configurationManager;
    private CORSManagementService corsManagementService;

    private CORSManagementServiceHolder() {

    }

    public static CORSManagementServiceHolder getInstance() {

        return SingletonHelper.INSTANCE;
    }

    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    public CORSManagementService getCorsManagementService() {

        return corsManagementService;
    }

    public void setCorsManagementService(CORSManagementService corsManagementService) {

        this.corsManagementService = corsManagementService;
    }

    /**
     * SingletonHelper for the singleton instance of CORSServiceHolder.
     */
    private static class SingletonHelper {

        private static final CORSManagementServiceHolder INSTANCE = new CORSManagementServiceHolder();
    }
}

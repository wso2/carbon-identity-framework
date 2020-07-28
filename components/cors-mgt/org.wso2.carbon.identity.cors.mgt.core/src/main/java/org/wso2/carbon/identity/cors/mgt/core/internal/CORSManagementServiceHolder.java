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
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSConfigurationDAO;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSOriginDAO;

import java.util.ArrayList;
import java.util.List;

/**
 * Service holder class for CORS-Service.
 */
public class CORSManagementServiceHolder {

    private List<CORSOriginDAO> corsOriginDAOS = new ArrayList<>();
    private List<CORSConfigurationDAO> corsConfigurationDAOS = new ArrayList<>();
    private ConfigurationManager configurationManager;

    private CORSManagementServiceHolder() {

    }

    public static CORSManagementServiceHolder getInstance() {

        return SingletonHelper.INSTANCE;
    }

    public List<CORSOriginDAO> getCorsOriginDAOS() {

        return corsOriginDAOS;
    }

    public void setCorsOriginDAOS(List<CORSOriginDAO> corsOriginDAOS) {

        this.corsOriginDAOS = corsOriginDAOS;
    }

    public List<CORSConfigurationDAO> getCorsConfigurationDAOS() {

        return corsConfigurationDAOS;
    }

    public void setCorsConfigurationDAOS(List<CORSConfigurationDAO> corsConfigurationDAOS) {

        this.corsConfigurationDAOS = corsConfigurationDAOS;
    }

    public ConfigurationManager getConfigurationManager() {

        return configurationManager;
    }

    public void setConfigurationManager(ConfigurationManager configurationManager) {

        this.configurationManager = configurationManager;
    }

    /**
     * SingletonHelper for the singleton instance of CORSServiceHolder.
     */
    private static class SingletonHelper {

        private static final CORSManagementServiceHolder INSTANCE = new CORSManagementServiceHolder();
    }
}

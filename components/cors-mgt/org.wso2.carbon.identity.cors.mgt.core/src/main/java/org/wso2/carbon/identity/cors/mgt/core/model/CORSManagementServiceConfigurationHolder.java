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
 *
 * NOTE: The code/logic in this class is copied from https://bitbucket.org/thetransactioncompany/cors-filter.
 * All credits goes to the original authors of the project https://bitbucket.org/thetransactioncompany/cors-filter.
 */

package org.wso2.carbon.identity.cors.mgt.core.model;

import org.wso2.carbon.identity.cors.mgt.core.dao.CORSConfigurationDAO;
import org.wso2.carbon.identity.cors.mgt.core.dao.CORSOriginDAO;

import java.util.List;

/**
 * This class represents the configuration data holder for the CORSManagementService class.
 */
public class CORSManagementServiceConfigurationHolder {

    private List<CORSOriginDAO> corsOriginDAOS;
    private List<CORSConfigurationDAO> corsConfigurationDAOS;

    public List<CORSOriginDAO> getCorsOriginDAOS() {

        return corsOriginDAOS;
    }

    public void setCorsOriginDAOS(List<CORSOriginDAO> corsOriginDAOS) {

        this.corsOriginDAOS = corsOriginDAOS;
    }

    public List<CORSConfigurationDAO> getCorsConfigurationDAOS() {

        return corsConfigurationDAOS;
    }

    public void setCorsConfigurationDAOS(
            List<CORSConfigurationDAO> corsConfigurationDAOS) {

        this.corsConfigurationDAOS = corsConfigurationDAOS;
    }
}

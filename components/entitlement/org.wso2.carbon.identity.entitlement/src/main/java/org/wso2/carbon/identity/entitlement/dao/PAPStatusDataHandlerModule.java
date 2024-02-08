/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.entitlement.dao;

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;

import java.util.List;
import java.util.Properties;

/**
 * This listener would be fired after an admin action is done
 */
public interface PAPStatusDataHandlerModule {

    /**
     * init entitlement status data handler module
     */
    void init(Properties properties);


    /**
     * Handles
     */
    void handle(String about, String key, List<StatusHolder> statusHolder) throws EntitlementException;


    /**
     * Handles
     */
    void handle(String about, StatusHolder statusHolder) throws EntitlementException;


    /**
     * Returns status data
     */
    StatusHolder[] getStatusData(String about, String key, String type, String searchString)
            throws EntitlementException;

}

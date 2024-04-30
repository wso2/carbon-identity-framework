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

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dto.StatusHolder;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Properties;
import java.util.List;


/**
 * This interface supports the management of status data (audit logs).
 */
public interface StatusDataStore {


    default void init(Properties properties) {

    }


    /**
     * Handles policy status data
     */
    void handlePolicyStatus(String policyId, List<StatusHolder> statusHolders) throws EntitlementException;


    /**
     * Handles subscriber status data
     */
    void handleSubscriberStatus(String policyId, List<StatusHolder> statusHolders) throws EntitlementException;


    /**
     * Gets the requested policy status data
     */
    StatusHolder[] getPolicyStatus(String policyId, String type, String filter) throws EntitlementException;


    /**
     * Gets the requested subscriber status data
     */
    StatusHolder[] getSubscriberStatus(String policyId, String type, String filter) throws EntitlementException;


    /**
     * Removes status data
     */
    void removeStatusData(String path) throws EntitlementException;
}

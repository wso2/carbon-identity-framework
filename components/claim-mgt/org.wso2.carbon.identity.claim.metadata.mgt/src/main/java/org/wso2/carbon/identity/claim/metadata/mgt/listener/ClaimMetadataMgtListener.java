/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.claim.metadata.mgt.listener;

import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;

/**
 * Definition for the listeners which listens to Claim CRUD events.
 */
public interface ClaimMetadataMgtListener {

    /**
     * Get the execution order identifier for this listener.
     *
     * @return The execution order identifier integer value.
     */
    int getExecutionOrderId();

    /**
     * Get the default order identifier for this listener.
     *
     * @return Default order id.
     */
    int getDefaultOrderId();

    /**
     * Check whether the listener is enabled or not.
     *
     * @return True if enabled.
     */
    boolean isEnable();

    /**
     * Method which carries out tasks which should run before deleting a claim.
     *
     * @param claimUri      Claim URI.
     * @param tenantDomain  Tenant domain.
     * @return  True if method executes successfully.
     * @throws ClaimMetadataException   ClaimMetadataException error.
     */
    boolean doPreDeleteClaim(String claimUri, String tenantDomain) throws ClaimMetadataException;

    /**
     * Method which carries out tasks which should run after deleting a claim.
     *
     * @param claimUri      Claim URI.
     * @param tenantDomain  Tenant domain.
     * @return  True if method executes successfully.
     * @throws ClaimMetadataException   ClaimMetadataException error.
     */
    boolean doPostDeleteClaim(String claimUri, String tenantDomain) throws ClaimMetadataException;

    /**
     * Method which carries out tasks which should run before updating a claim.
     *
     * @param localClaim    Local claim.
     * @param tenantDomain  Tenant domain.
     * @return  True if method executes successfully.
     * @throws ClaimMetadataException   ClaimMetadataException error.
     */
    boolean doPreUpdateLocalClaim(LocalClaim localClaim, String tenantDomain) throws ClaimMetadataException;
}

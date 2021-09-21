/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
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

package org.wso2.carbon.identity.claim.metadata.mgt.dao;

import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.user.core.claim.inmemory.ClaimConfig;

/**
 * Initialize the claim configuration of tenant to the tables.
 */
public interface ClaimConfigInitDAO {

    /**
     * Initialize the claim configuration of tenant to the database.
     *
     * @param claimConfig Claim Configuration.
     * @param tenantId    Tenant Id.
     * @throws ClaimMetadataException Claim Meta data Exception.
     */
    void initClaimConfig(ClaimConfig claimConfig, int tenantId) throws ClaimMetadataException;
}

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.claim.mgt;

import org.wso2.carbon.identity.claim.mgt.model.ClaimMapping;
import org.wso2.carbon.identity.claim.mgt.model.ClaimToClaimMapping;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.Map;
import java.util.Set;


public interface ClaimManager extends org.wso2.carbon.user.core.claim.ClaimManager {

    /**
     * Gets all supported claims by default in the system.
     *
     * @return An array of claim objects supported by default
     * @throws UserStoreException
     */
    ClaimMapping[] getAllSupportClaimMappingsByDefault() throws UserStoreException;

    /**
     * Gets all mandatory claims
     *
     * @return An array of required claim objects
     * @throws UserStoreException
     */
    ClaimMapping[] getAllRequiredClaimMappings() throws UserStoreException;

    /**
     * Gets all claims with the relation between local claims and additional claims
     *
     * @return An array of claimToClaimMappin objects
     * @throws UserStoreException
     */
    ClaimToClaimMapping[] getAllClaimToClaimMappings() throws UserStoreException;

    Map<String, String> getMappingsMapFromOtherDialectToCarbon(String otherDialectURI, Set<String>
            otherClaimURIs, boolean useCarbonDialectAsKey) throws UserStoreException;

}

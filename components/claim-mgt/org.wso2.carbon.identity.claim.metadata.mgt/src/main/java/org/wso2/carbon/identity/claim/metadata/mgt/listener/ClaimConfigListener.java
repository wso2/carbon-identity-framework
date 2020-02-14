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
 */

package org.wso2.carbon.identity.claim.metadata.mgt.listener;

import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.internal.IdentityClaimManagementServiceDataHolder;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;
import org.wso2.carbon.user.api.UserStoreException;

/**
 * Class which contains the implementation of a listener for claim configurations. This listener is responsible for
 * actions related to claim configurations when the user store change happens.
 */
public class ClaimConfigListener implements UserStoreConfigListener {

    @Override
    public void onUserStoreNamePreUpdate(int tenantId, String currentUserStoreName, String newUserStoreName) throws
            UserStoreException {

        // Not Implemented.
    }

    @Override
    public void onUserStoreNamePostUpdate(int tenantId, String currentUserStoreName, String newUserStoreName) throws
            UserStoreException {

        // Not Implemented.
    }

    @Override
    public void onUserStorePreDelete(int tenantId, String userstoreDomain) throws UserStoreException {

        try {
            IdentityClaimManagementServiceDataHolder.getInstance().getClaimManagementService()
                    .removeClaimMappingAttributes(tenantId, userstoreDomain);
        } catch (ClaimMetadataException e) {
            throw new UserStoreException(e.getMessage(), e);
        }
    }

    @Override
    public void onUserStorePostDelete(int tenantId, String userStoreName) throws UserStoreException {

        // Not implemented.
    }
}

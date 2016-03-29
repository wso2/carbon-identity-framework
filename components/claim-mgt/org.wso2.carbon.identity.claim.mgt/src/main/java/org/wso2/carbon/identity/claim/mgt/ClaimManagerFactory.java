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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.HashMap;
import java.util.Map;

public class ClaimManagerFactory implements org.wso2.carbon.user.core.claim.ClaimManagerFactory {

    public static final Log log = LogFactory.getLog(ClaimManagerFactory.class);
    private static Map<Integer, IdentityMgtClaimManager> claimManagerMap = new HashMap();

    public ClaimManagerFactory() {

    }

    public IdentityMgtClaimManager getClaimManager(int tenantId) {
        if (claimManagerMap.get(tenantId)!=null){
            return claimManagerMap.get(tenantId);
        }
        try {
            IdentityMgtClaimManager identityMgtClaimManager = new IdentityMgtClaimManager(tenantId);
            claimManagerMap.put(tenantId, identityMgtClaimManager);
            return identityMgtClaimManager;
        } catch (UserStoreException e) {
            log.error("Error when creating IdentityMgtClaimManager instance " + e);
            return null;
        }
    }
}

/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.oauth.endpoint.user.impl;

import org.apache.commons.collections.MapUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.oauth.user.UserInfoClaimRetriever;

import java.util.HashMap;
import java.util.Map;

/**
 * Retrieving claims from the user store for the given claims dialect
 */
public class UserInfoUserStoreClaimRetriever implements UserInfoClaimRetriever {

    @Override
    public Map<String, Object> getClaimsMap(Map<ClaimMapping, String> userAttributes) {
        Map<String, Object> claims = new HashMap<String, Object>();
        if (MapUtils.isNotEmpty(userAttributes)) {
            for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()){
                if (IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR.equals(entry.getKey().getRemoteClaim()
                        .getClaimUri())){
                    continue;
                }
                claims.put(entry.getKey().getRemoteClaim().getClaimUri(), entry.getValue());
            }
        }
        return claims;
    }

}

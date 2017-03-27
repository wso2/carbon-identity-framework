/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.model;

import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.util.HashSet;
import java.util.Set;

public class LocalUser extends User {

    private static final long serialVersionUID = -6463173506025377046L;

    private org.wso2.carbon.identity.mgt.User user;

    public LocalUser(org.wso2.carbon.identity.mgt.User user) {
        this.user = user;
    }

    @Override
    public Set<Claim> getClaims() throws IdentityStoreException, UserNotFoundException {
        return new HashSet(user.getClaims());
    }

    @Override
    public String getUserIdentifier() {
        return user.getUniqueUserId();
    }
}
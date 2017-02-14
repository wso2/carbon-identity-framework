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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.identity.gateway.api.FrameworkRuntimeException;
import org.wso2.carbon.identity.gateway.api.internal.GatewayResourceAPIComponent;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;

import java.util.HashSet;
import java.util.Set;

public class LocalUser extends User {

    private Logger log = LoggerFactory.getLogger(LocalUser.class);

    org.wso2.carbon.identity.mgt.User user;

    public LocalUser(org.wso2.carbon.identity.mgt.User user) {
        this.user = user;
    }

    @Override
    public String getUserIdentifier() {
        return user.getUniqueUserId();
    }

    @Override
    public Set<Claim> getClaims() {
        try {
            return new HashSet(user.getClaims());
        } catch (IdentityStoreException e) {
            log.error("Error while reading user claims from local identity store.", e);
            return new HashSet();
        } catch (UserNotFoundException e) {
            throw new FrameworkRuntimeException("User cannot be found in local identity store.", e);
        }
    }
}
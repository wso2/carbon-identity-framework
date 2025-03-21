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

package org.wso2.carbon.identity.action.execution.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class models the User.
 * User is the entity that represents the user for whom the action is triggered for.
 */
public class User {

    private String id;
    private final List<UserClaim> claims =  new ArrayList<>();

    public User(String id) {

        this.id = id;
    }

    public String getId() {

        return id;
    }

    public void setClaims(UserClaim userClaim) {
        claims.add(userClaim);
    }

    public List<UserClaim> getClaims() {
        return claims;
    }
}

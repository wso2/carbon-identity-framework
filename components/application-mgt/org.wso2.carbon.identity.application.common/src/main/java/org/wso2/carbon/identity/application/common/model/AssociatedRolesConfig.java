/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO : support xml based SP import/export

/**
 * Associated v2 roles for the application.
 */
public class AssociatedRolesConfig implements Serializable {

    private static final long serialVersionUID = 497647508006862448L;

    // TODO: use the enum defined in roles component
    private String allowedAudience;
    private List<RoleV2> roles;

    public String getAllowedAudience() {

        return allowedAudience;
    }

    public void setAllowedAudience(String allowedAudience) {

        this.allowedAudience = allowedAudience;
    }

    public List<RoleV2> getRoles() {

        if (roles == null) {
            return new ArrayList<>();
        }
        return roles;
    }

    public void setRoles(List<RoleV2> roles) {

        this.roles = roles;
    }
}

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

package org.wso2.carbon.identity.role.v2.mgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.role.mgt.core.RoleConstants;

import java.util.Locale;

/**
 * Util class for role management functionality.
 */
public class RoleManagementUtils {

    private Log log = LogFactory.getLog(RoleManagementUtils.class);

    /**
     * Checks whether the given role is an internal or application role.
     *
     * @param roleName Role name.
     * @return Whether the passed role is "internal" or "application".
     */
    public static boolean isHybridRole(String roleName) {

        return roleName.toLowerCase(Locale.ENGLISH).startsWith((RoleConstants.INTERNAL_DOMAIN +
                CarbonConstants.DOMAIN_SEPARATOR).toLowerCase(Locale.ENGLISH)) ||
                roleName.toLowerCase(Locale.ENGLISH).startsWith((RoleConstants.APPLICATION_DOMAIN +
                        CarbonConstants.DOMAIN_SEPARATOR).toLowerCase(Locale.ENGLISH));
    }
}

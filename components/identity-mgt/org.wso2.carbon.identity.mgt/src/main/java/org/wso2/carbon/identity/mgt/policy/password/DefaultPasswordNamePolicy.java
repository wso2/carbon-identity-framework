/*
 * Copyright (c) 2014-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.mgt.policy.password;

import org.apache.commons.collections.MapUtils;
import org.wso2.carbon.identity.mgt.policy.AbstractPasswordPolicyEnforcer;

import java.util.Map;

public class DefaultPasswordNamePolicy extends AbstractPasswordPolicyEnforcer {

    private static final String EQUAL_MODE = "equal";
    private static final String CONTAIN_MODE = "contain";

    private String usernameCheckMode = EQUAL_MODE;

    @Override
    public boolean enforce(Object... args) {

        if (args != null) {

            String password = args[0].toString();
            String username = args[1].toString();

            if (CONTAIN_MODE.equalsIgnoreCase(usernameCheckMode)) {
                if (password.toLowerCase().contains(username.toLowerCase())) {
                    errorMessage = "Password cannot contain the username";
                    return false;
                } else {
                    return true;
                }
            } else {
                if (password.equalsIgnoreCase(username)) {
                    errorMessage = "Cannot use the username as the password";
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return true;
        }
    }

    @Override
    public void init(Map<String, String> params) {

        // Initialize the configuration with the parameters defined in config file.
        if (!MapUtils.isEmpty(params)) {
            usernameCheckMode = params.get("username.check.mode");
        }
    }

}

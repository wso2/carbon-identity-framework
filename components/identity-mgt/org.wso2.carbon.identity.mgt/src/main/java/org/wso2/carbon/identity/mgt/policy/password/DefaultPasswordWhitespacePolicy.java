/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

import org.wso2.carbon.identity.mgt.policy.AbstractPasswordPolicyEnforcer;

import java.util.Map;

public class DefaultPasswordWhitespacePolicy extends AbstractPasswordPolicyEnforcer {

    @Override
    public boolean enforce(Object... args) {

        if (args != null) {

            String password = args[0].toString();
            String[] splits = password.split(" ");
            if (splits.length > 1) {
                errorMessage = "Password cannot contain whitespaces";
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void init(Map<String, String> params) {
        // Nothing to init.

    }

}

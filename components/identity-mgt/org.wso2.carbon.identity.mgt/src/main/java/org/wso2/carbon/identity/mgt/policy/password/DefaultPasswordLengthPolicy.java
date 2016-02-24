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

import org.apache.commons.collections.MapUtils;
import org.wso2.carbon.identity.mgt.policy.AbstractPasswordPolicyEnforcer;

import java.util.Map;

/**
 * This class is used to enforce the password length policy. This checks minimum and
 * maximum lengths of the password.
 */
public class DefaultPasswordLengthPolicy extends AbstractPasswordPolicyEnforcer {

    private int MIN_LENGTH = 6;
    private int MAX_LENGTH = 10;

    /**
     * Required initializations to get the configuration values from file.
     */
    @Override
    public void init(Map<String, String> params) {

		/*
         *  Initialize the configuration with the parameters defined in config file.
		 *  Eg.
		 *  In the config file if you specify as follows.
		 *  Password.policy.extensions.1.min.length=6
		 *  Get the value from the map as shown below using key "min.length".
		 */
        if (!MapUtils.isEmpty(params)) {
            MIN_LENGTH = Integer.parseInt(params.get("min.length"));
            MAX_LENGTH = Integer.parseInt(params.get("max.length"));
        }
    }

    /**
     * Policy enforcing method.
     *
     * @param - the first parameter assumed to be the password. The order of the parameters
     *          are implementation dependent.
     */
    @Override
    public boolean enforce(Object... args) {

        // If null input pass through.
        if (args != null) {

            String password = args[0].toString();
            if (password.length() < MIN_LENGTH) {
                errorMessage = "Password at least should have " + MIN_LENGTH + " characters";
                return false;
            } else if (password.length() > MAX_LENGTH) {
                errorMessage = "Password cannot have more than " + MAX_LENGTH + " characters";
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


}

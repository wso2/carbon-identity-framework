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

package org.wso2.carbon.identity.mgt.policy;

import java.util.Map;

/**
 * This is the interface to be used by custom policy implementations such as password policy
 * enforcement.
 */
public interface PolicyEnforcer {

    /**
     * This method is used to enforce the policy forcing it to apply it and validate the outcome.
     * It returns true if the policy enforcement is successful and no violations have been occured.
     * A false return means the perticular policy have been violated and no more processing needs
     * to be done.
     *
     * @param args - arguments to the policy implementer. Order is implementation dependant.
     * @return - true if policy enforcement success. false if violated.
     */
    boolean enforce(Object... args);

    /**
     * Descriptive error message about the policy violation. Implementor should give descriptive
     * message.
     *
     * @return - error string
     */
    String getErrorMessage();

    /**
     * This method is used to initialize the policy implementation using a Map.
     * You can give parameters to the implementation class as shown below in the config file.
     * Eg.
     * Password.policy.extensions.1.min.length=6
     * To initialize the implementation class, access the Map with the key after sequence
     * as follows.
     * String minLength = params.get("min.length");
     */
    void init(Map<String, String> params);
}

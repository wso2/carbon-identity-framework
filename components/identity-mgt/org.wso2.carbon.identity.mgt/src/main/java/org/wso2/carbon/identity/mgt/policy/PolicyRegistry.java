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

import java.util.ArrayList;
import java.util.List;

public class PolicyRegistry {

    private List<PolicyEnforcer> policyCollection = new ArrayList<PolicyEnforcer>();

    public PolicyRegistry() {

    }

    public void enforcePasswordPolicies(Object... args) throws PolicyViolationException {

        if (args != null) {

            for (PolicyEnforcer policy : policyCollection) {

                if (policy instanceof AbstractPasswordPolicyEnforcer && !policy.enforce(args)) {
                    throw new PolicyViolationException(policy.getErrorMessage());
                }

            }
        }
    }

    public void addPolicy(PolicyEnforcer policy) {

        policyCollection.add(policy);
    }
}

/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.webhook.metadata.api.model;

import org.wso2.carbon.identity.organization.resource.sharing.policy.management.constant.PolicyEnum;

/**
 * Wrapper class for PolicyEnum to expose policyCode, policyName, and the full PolicyEnum.
 */
public class OrganizationPolicy {

    private final String policyCode;
    private final String policyName;
    private final String policyValue;
    private final PolicyEnum policyEnum;

    public OrganizationPolicy(PolicyEnum policyEnum) {

        this.policyEnum = policyEnum;
        this.policyCode = policyEnum.getPolicyCode();
        this.policyName = policyEnum.getPolicyName();
        this.policyValue = policyEnum.getValue();
    }

    public String getPolicyCode() {

        return policyCode;
    }

    public String getPolicyName() {

        return policyName;
    }

    public PolicyEnum getPolicyEnum() {

        return policyEnum;
    }

    public String getPolicyValue() {

        return policyValue;
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.policy.management.api.model;

/**
 * Lightweight summary of a Policy for list views.
 * Carries only the fields a list page needs (id and name); the full {@link Policy} with hydrated
 * resources is retrieved via {@code getPolicyById}.
 */
public class PolicyBasicInfo {

    private final String id;
    private final String name;

    public PolicyBasicInfo(String id, String name) {

        this.id = id;
        this.name = name;
    }

    /**
     * Returns the policy ID.
     *
     * @return Policy ID.
     */
    public String getId() {

        return id;
    }

    /**
     * Returns the policy name.
     *
     * @return Policy name.
     */
    public String getName() {

        return name;
    }
}

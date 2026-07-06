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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a policy.
 */
public class Policy {

    private final String id;
    private final String name;
    private final String tenantDomain;
    private final List<PolicyResource> resources;

    public Policy(String id, String name, String tenantDomain, List<PolicyResource> resources) {

        this.id = id;
        this.name = name;
        this.tenantDomain = tenantDomain;
        this.resources = resources != null
                ? Collections.unmodifiableList(new ArrayList<>(resources)) : Collections.emptyList();
    }

    public String getId() {

        return id;
    }

    public String getName() {

        return name;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public List<PolicyResource> getResources() {

        return resources;
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.flow.execution.engine.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the organization details collected during a flow execution.
 * <p>
 * Inputs whose {@code identifierType} marks them as organization data are routed here by the flow
 * engine, so that an executor can create or update an organization without inspecting raw user input.
 * The name, handle and description are first class fields because the engine and its executors
 * reference them directly; anything else the flow collects is kept in {@link #getAttributes()}.
 */
public class FlowOrganization implements Serializable {

    private static final long serialVersionUID = -6069004467814244409L;

    private String organizationName;
    private String organizationHandle;
    private String organizationDescription;
    private String organizationStatus;
    private Map<String, String> attributes = new HashMap<>();

    public String getOrganizationName() {

        return organizationName;
    }

    public void setOrganizationName(String organizationName) {

        this.organizationName = organizationName;
    }

    public String getOrganizationHandle() {

        return organizationHandle;
    }

    public void setOrganizationHandle(String organizationHandle) {

        this.organizationHandle = organizationHandle;
    }

    public String getOrganizationDescription() {

        return organizationDescription;
    }

    public void setOrganizationDescription(String organizationDescription) {

        this.organizationDescription = organizationDescription;
    }

    public String getOrganizationStatus() {

        return organizationStatus;
    }

    public void setOrganizationStatus(String organizationStatus) {

        this.organizationStatus = organizationStatus;
    }

    public Map<String, String> getAttributes() {

        return attributes;
    }

    public String getAttribute(String key) {

        return attributes.get(key);
    }

    public void setAttribute(String key, String value) {

        this.attributes.put(key, value);
    }
}

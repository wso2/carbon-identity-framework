/**
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.debug.framework.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a debug request with resource information.
 * This model provides type-safe access to debug request parameters.
 */
public class DebugRequest {

    private String connectionId;
    private String resourceType;
    private final Map<String, Object> additionalContext;

    /**
     * Constructs an empty DebugRequest.
     */
    public DebugRequest() {

        this.additionalContext = new HashMap<>();
    }

    public String getConnectionId() {

        return connectionId;
    }

    public void setConnectionId(String connectionId) {

        this.connectionId = connectionId;
    }

    public String getResourceType() {

        return resourceType;
    }

    public void setResourceType(String resourceType) {

        this.resourceType = resourceType;
    }

    public void addContextProperty(String key, Object value) {

        this.additionalContext.put(key, value);
    }

    public Map<String, Object> getAdditionalContext() {

        return Collections.unmodifiableMap(additionalContext);
    }
}

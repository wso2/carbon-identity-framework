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

package org.wso2.carbon.identity.flow.extension.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain model for a context path with an optional encryption flag.
 * Used for both expose and modify path entries in {@link AccessConfig}.
 * <p>
 * When {@code encrypted} is {@code true}, the value at this path prefix is JWE-encrypted:
 * for expose paths, outbound values are encrypted with the external service's certificate;
 * for modify paths, inbound values from the external service are encrypted with IS's key.
 * </p>
 * <p>
 * A runtime value or request header entry names the flow type it applies to, as each flow defines its own. An entry
 * of the organization a flow onboards names the {@code REGISTRATION} flow type, as only that flow onboards one.
 * Every other entry has no flow type and applies to all flows.
 * </p>
 */
public class ContextPath {

    private final String path;
    private final boolean encrypted;
    private final String flowType;

    public ContextPath(String path, boolean encrypted) {

        this(path, encrypted, null);
    }

    @JsonCreator
    public ContextPath(@JsonProperty("path") String path,
                       @JsonProperty("encrypted") boolean encrypted,
                       @JsonProperty("flowType") String flowType) {

        this.path = path;
        this.encrypted = encrypted;
        this.flowType = flowType;
    }

    /**
     * Returns the hierarchical path prefix (e.g. {@code "/user/credentials/"}).
     *
     * @return The path string.
     */
    public String getPath() {

        return path;
    }

    /**
     * Returns whether values at this path should be JWE-encrypted before sending
     * to the external service.
     *
     * @return {@code true} if encryption is enabled for this path.
     */
    public boolean isEncrypted() {

        return encrypted;
    }

    /**
     * Returns the flow type this entry applies to.
     *
     * @return The flow type, or {@code null} when the entry applies to all flows.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getFlowType() {

        return flowType;
    }
}

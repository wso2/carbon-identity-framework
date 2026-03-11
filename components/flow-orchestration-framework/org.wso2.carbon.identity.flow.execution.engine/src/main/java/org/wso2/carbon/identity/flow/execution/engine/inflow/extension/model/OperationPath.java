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

package org.wso2.carbon.identity.flow.execution.engine.inflow.extension.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Domain model for an operation path with an optional encryption flag.
 * <p>
 * Used within {@link AllowedOperation} to represent a single path on which
 * the operation is allowed, along with an encryption flag indicating whether
 * the IS should expect/produce JWE-encrypted values for that specific path.
 * </p>
 */
public class OperationPath {

    private final String path;
    private final boolean encrypted;

    @JsonCreator
    public OperationPath(@JsonProperty("path") String path,
                         @JsonProperty("encrypted") boolean encrypted) {

        this.path = path;
        this.encrypted = encrypted;
    }

    /**
     * Returns the operation path (e.g. {@code "/user/claims/"}, {@code "/properties/riskScore"}).
     *
     * @return The path string.
     */
    public String getPath() {

        return path;
    }

    /**
     * Returns whether this operation path requires JWE encryption/decryption.
     * <ul>
     *   <li><b>ADD</b> operations: external service sends JWE-encrypted values to the IS.</li>
     *   <li><b>REPLACE</b> operations: both directions — outbound current value encrypted,
     *       inbound replacement value encrypted.</li>
     *   <li><b>REMOVE</b> operations: outbound current value encrypted in the expose event.</li>
     * </ul>
     *
     * @return {@code true} if JWE encryption is required for this path.
     */
    public boolean isEncrypted() {

        return encrypted;
    }
}

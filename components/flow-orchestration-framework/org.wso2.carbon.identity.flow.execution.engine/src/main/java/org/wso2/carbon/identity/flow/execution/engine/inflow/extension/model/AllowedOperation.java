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

import java.util.Collections;
import java.util.List;

/**
 * Domain model for an allowed operation with structured paths.
 * <p>
 * Each entry groups an operation type with a list of {@link OperationPath} entries,
 * where each path carries its own {@code encrypted} flag.
 * </p>
 *
 * <p>Example JSON:</p>
 * <pre>{@code
 * {
 *   "op": "REPLACE",
 *   "paths": [
 *     { "path": "/user/claims/", "encrypted": false },
 *     { "path": "/user/credentials/password", "encrypted": true }
 *   ]
 * }
 * }</pre>
 */
public class AllowedOperation {

    private final String op;
    private final List<OperationPath> paths;

    @JsonCreator
    public AllowedOperation(@JsonProperty("op") String op,
                            @JsonProperty("paths") List<OperationPath> paths) {

        this.op = op;
        this.paths = paths != null ? Collections.unmodifiableList(paths) : Collections.emptyList();
    }

    /**
     * Returns the operation type (e.g. {@code "ADD"}, {@code "REPLACE"}, {@code "REMOVE"}).
     *
     * @return The operation type string.
     */
    public String getOp() {

        return op;
    }

    /**
     * Returns the list of {@link OperationPath} entries for this operation.
     *
     * @return Unmodifiable list of operation paths with encryption flags.
     */
    public List<OperationPath> getPaths() {

        return paths;
    }

    /**
     * Returns whether any path in this operation has encryption enabled.
     *
     * @return {@code true} if at least one path has {@code encrypted = true}.
     */
    public boolean hasAnyEncryptedPath() {

        return paths.stream().anyMatch(OperationPath::isEncrypted);
    }
}

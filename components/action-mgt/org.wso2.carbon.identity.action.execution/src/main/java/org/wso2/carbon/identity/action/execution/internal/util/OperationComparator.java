/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.execution.internal.util;

import org.wso2.carbon.identity.action.execution.api.model.AllowedOperation;
import org.wso2.carbon.identity.action.execution.api.model.Operation;
import org.wso2.carbon.identity.action.execution.api.model.PerformableOperation;

/**
 * This class compares an allowed operation against a performable operation to determine if the latter is permitted.
 * The comparison between an {@link AllowedOperation} and a {@link PerformableOperation} ensures that only authorized
 * modifications are made during the execution of an action. This class facilitates the validation of performable
 * operations against a set of predefined allowed operations, based on the action type.
 *
 * <p>Key aspects of the comparison include:</p>
 * <ul>
 *     <li>Equality of operation types (e.g., "add", "remove", "replace") as defined in the JSON Patch
 *     specification (RFC 6902).</li>
 *     <li>Matching of operation paths, considering both exact matches and base path matches to allow
 *     for flexibility in specifying allowed operations.</li>
 * </ul>
 */
public class OperationComparator {

    public static boolean compare(AllowedOperation allowedOp, PerformableOperation performableOp) {

        if (!allowedOp.getOp().equals(performableOp.getOp())) {
            return false;
        }

        if (Operation.REDIRECT.equals(performableOp.getOp())) {
            return true;
        }

        String performableOperationBasePath = performableOp.getPath().contains("/")
                ? performableOp.getPath().substring(0, performableOp.getPath().lastIndexOf('/') + 1)
                : "";

        for (String allowedPath : allowedOp.getPaths()) {
            if (performableOp.getPath().equals(allowedPath) ||
                    performableOperationBasePath.equals(allowedPath)) {
                return true;
            }
        }

        return false;
    }
}

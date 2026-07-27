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

package org.wso2.carbon.identity.action.execution.api.model;

import java.util.List;

/**
 * This class models the Allowed Operation.
 * Allowed Operation is the operation that is allowed to be performed during the execution of a particular action.
 * The operation is defined by the operation type and the paths that the operation is allowed to be performed on.
 * Allowed operations follow JSON Patch format (RFC 6902) semantics to define the operations that are allowed to be
 * performed.
 */
public class AllowedOperation {

    private Operation op;

    private List<String> paths;

    public Operation getOp() {

        return op;
    }

    public void setOp(Operation op) {

        this.op = op;
    }

    public List<String> getPaths() {

        return paths;
    }

    public void setPaths(List<String> paths) {

        this.paths = paths;
    }
}

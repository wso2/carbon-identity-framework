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

/**
 * This class models the Performable Operation.
 * Performable Operation is the operation that is requested to be performed in the system during the execution of an
 * action. It contains the operation type, the path of the operation and the value associated with add or replace
 * operations.
 * Performable operations are expected to follow the JSON Patch format (RFC 6902), providing a standardized way to
 * express the operations to be performed.
 */
public class PerformableOperation {

    private Operation op;
    private String path;
    private Object value;
    private String url;

    public Operation getOp() {

        return op;
    }

    public void setOp(Operation op) {

        this.op = op;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        if (Operation.REDIRECT.equals(op)) {
            throw new IllegalArgumentException("Path is not allowed for REDIRECT operation.");
        }
        this.path = path;
    }

    public Object getValue() {

        return value;
    }

    public void setValue(Object value) {

        if (Operation.REDIRECT.equals(op)) {
            throw new IllegalArgumentException("Value is not allowed for REDIRECT operation.");
        }
        this.value = value;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        if (!Operation.REDIRECT.equals(op)) {
            throw new IllegalArgumentException("Url is only allowed for REDIRECT operation.");
        }
        this.url = url;
    }
}

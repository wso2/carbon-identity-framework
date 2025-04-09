/*
 * Copyright (c) 2023-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.framework.async.status.mgt.api.buffer;

/**
 * Represents the status of a single sub-operation within an asynchronous operation.
 * This object holds a status value (e.g., "SUCCESS" or "FAIL") that can be used
 * to evaluate the overall operation's result.
 */
public class SubOperationStatusObject {

    private String status;

    public SubOperationStatusObject(String status) {

        this.status = status;
    }

    public SubOperationStatusObject() {

        this.status = null;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }
}

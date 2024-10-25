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

package org.wso2.carbon.identity.action.execution.model;

/**
 * This class models the Failure entity.
 * This captures the failure reason and message communicated by the external service implementing the extension.
 */
public class Failure {

    private final String reason;
    private final String description;

    public Failure(String reason, String description) {

        this.reason = reason;
        this.description = description;
    }

    public String getReason() {

        return reason;
    }

    public String getDescription() {

        return description;
    }
}

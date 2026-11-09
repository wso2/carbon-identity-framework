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

package org.wso2.carbon.identity.flow.execution.engine.poll;

import java.util.Objects;

/**
 * A stored polling entry. The expiry is in epoch milliseconds, or 0 if unknown, and then it is not cached.
 */
public class FlowPollingEntry {

    private final String flowId;
    private final String status;
    private final long expiresAt;

    public FlowPollingEntry(String flowId, String status, long expiresAt) {

        this.flowId = flowId;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public String getFlowId() {

        return flowId;
    }

    public String getStatus() {

        return status;
    }

    public long getExpiresAt() {

        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof FlowPollingEntry)) {
            return false;
        }
        FlowPollingEntry other = (FlowPollingEntry) o;
        return expiresAt == other.expiresAt && Objects.equals(flowId, other.flowId)
                && Objects.equals(status, other.status);
    }

    @Override
    public int hashCode() {

        return Objects.hash(flowId, status, expiresAt);
    }
}

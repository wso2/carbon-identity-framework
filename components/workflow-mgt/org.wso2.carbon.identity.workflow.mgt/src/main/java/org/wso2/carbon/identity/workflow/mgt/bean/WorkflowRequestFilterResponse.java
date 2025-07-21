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
package org.wso2.carbon.identity.workflow.mgt.bean;

/**
 * This object represents the response for a workflow request filter.
 */
public class WorkflowRequestFilterResponse {

    private WorkflowRequest[] requests;
    private int totalCount;

    public WorkflowRequestFilterResponse(WorkflowRequest[] requests, int totalCount) {

        this.requests = requests;
        this.totalCount = totalCount;
    }

    public void setRequests(WorkflowRequest[] requests) {

        this.requests = requests;
    }

    public WorkflowRequest[] getRequests() {

        return requests;
    }

    public int getTotalCount() {

        return totalCount;
    }

    public void setTotalCount(int totalCount) {

        this.totalCount = totalCount;
    }
}

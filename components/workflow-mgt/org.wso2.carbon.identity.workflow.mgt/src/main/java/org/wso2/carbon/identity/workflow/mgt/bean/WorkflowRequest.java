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

import java.util.ArrayList;
import java.util.List;

/**
 * This object represent a request associated with a workflow.
 */
public class WorkflowRequest {

    private String requestId;
    private String operationType;
    private String createdAt;
    private String updatedAt;
    private String status;
    private String requestParams;
    private String createdBy;
    private List<RequestParameter> requestParameters = new ArrayList<RequestParameter>();
    private List<Property> properties = new ArrayList<Property>();

    public String getRequestId() {

        return requestId;
    }

    public void setRequestId(String requestId) {

        this.requestId = requestId;
    }

    public String getOperationType() {

        return operationType;
    }

    public void setOperationType(String operationType) {

        this.operationType = operationType;
    }

    public String getRequestParams() {

        return requestParams;
    }

    public void setRequestParams(String requestParams) {

        this.requestParams = requestParams;
    }

    public String getUpdatedAt() {

        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {

        this.updatedAt = updatedAt;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public String getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(String createdAt) {

        this.createdAt = createdAt;
    }

    public String getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(String createdBy) {

        this.createdBy = createdBy;
    }

    /**
     * Get the list of properties.
     *
     * @return List of properties.
     */
    public List<Property> getProperties() {

        return properties;
    }

    /**
     * Set the list of properties.
     *
     * @param properties List of properties.
     */
    public void setProperties(List<Property> properties) {

        this.properties = properties;
    }

    /**
     * Set the list of request parameters.
     *
     * @param requestParameters List of request parameters.
     */
    public void setRequestParameters(List<RequestParameter> requestParameters) {

        this.requestParameters = requestParameters;
    }

    /**
     * Get the list of request parameters.
     *
     * @return List of request parameters.
     */
    public List<RequestParameter> getRequestParameters() {

        return requestParameters;
    }
}

/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.workflow.mgt.dto;

import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;

import java.io.Serializable;
import java.util.List;

public class WorkflowRequest implements Serializable, Cloneable {

    private static final long serialVersionUID = 578401681187017212L;

    public static final String CREDENTIAL = "Credential";
    private String uuid;
    private String eventType;
    private int tenantId;
    private List<RequestParameter> requestParameters;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public List<RequestParameter> getRequestParameters() {
        return requestParameters;
    }

    public void setRequestParameters(List<RequestParameter> requestParameters) {
        this.requestParameters = requestParameters;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "WorkFlowRequest{" +
                "uuid='" + uuid + "\'\n" +
                ", eventType='" + eventType + "\'\n" +
                ", tenantId=" + tenantId + '\n' +
                ", workflowParameters=" + requestParameters + '\n' +
                '}';
    }

    public String getRequestParameterAsString() {

        String requestParametersString = "{";
        for (int i = 0; i < requestParameters.size(); i++) {
            if(!CREDENTIAL.equals(requestParameters.get(i).getName())) {
                requestParametersString = requestParametersString + requestParameters.get(i).getName() + " : " +
                        requestParameters.get(i).getValue();
                if (i != requestParameters.size() - 1) {
                    requestParametersString = requestParametersString + ", \n";
                }
            }
        }
        requestParametersString = requestParametersString + "}";
        return requestParametersString;
    }

    @Override
    public WorkflowRequest clone() throws CloneNotSupportedException {

        return (WorkflowRequest) (super.clone());
    }
}

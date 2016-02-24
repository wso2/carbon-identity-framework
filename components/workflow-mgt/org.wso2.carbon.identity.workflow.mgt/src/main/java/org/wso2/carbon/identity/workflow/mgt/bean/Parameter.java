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

package org.wso2.carbon.identity.workflow.mgt.bean;

import java.io.Serializable;

/**
 * This is the Bean class that represent the Parameter per workflow.
 *
 * workflowId : Unique Id for the workflow
 * paramName : Parameter Name that is defined in the metafile
 * paramValue : Value for this parameter
 * qName :  Fully qualified name for this parameter. There can be more than one Parameter in same ParamName and but unique by qName.
 * holder : This is represent either template or workflowImpl
 *
 *
 */
public class Parameter implements Serializable {

    private static final long serialVersionUID = 7063803305644510640L;

    private String workflowId;
    private String paramName;
    private String paramValue;
    private String qName;
    private String holder;

    public Parameter() {
    }


    /**
     *
     * @param workflowId
     * @param paramName
     * @param paramValue
     * @param qName
     * @param holder
     */
    public Parameter(String workflowId, String paramName, String paramValue, String qName, String holder) {
        this.workflowId = workflowId;
        this.paramName = paramName;
        this.paramValue = paramValue;
        this.qName = qName;
        this.holder = holder;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getqName() {
        return qName;
    }

    public void setqName(String qName) {
        this.qName = qName;
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Parameter parameter = (Parameter) o;

        if (!workflowId.equals(parameter.workflowId)) return false;
        if (!paramName.equals(parameter.paramName)) return false;
        if (!qName.equals(parameter.qName)) return false;
        return holder.equals(parameter.holder);

    }

    @Override
    public int hashCode() {
        int result = workflowId.hashCode();
        result = 31 * result + paramName.hashCode();
        result = 31 * result + qName.hashCode();
        result = 31 * result + holder.hashCode();
        return result;
    }
}

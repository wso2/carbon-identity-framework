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

public class RequestParameter implements Serializable{

    private static final long serialVersionUID = -8564170214424881696L;

    private String name;
    private Object value;
    private String valueType;
    private boolean requiredInWorkflow;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public boolean isRequiredInWorkflow() {
        return requiredInWorkflow;
    }

    public void setRequiredInWorkflow(boolean requiredInWorkflow) {
        this.requiredInWorkflow = requiredInWorkflow;
    }

    @Override
    public String toString() {
        return "WorkflowParameter{" +
                "name='" + name + "\'\n" +
                ", value=" + value + "\n" +
                ", valueType='" + valueType + "\'\n" +
                ", requiredInWorkflow=" + requiredInWorkflow + "\n" +
                '}';
    }
}

/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.flow.mgt.model;

/**
 * A variable a flow defines. Unlike a header or query parameter, it may start with a value, and may be updated by
 * the user.
 */
public class FlowVariableInputDTO extends FlowInputDTO {

    private static final long serialVersionUID = 1L;

    private Object value;
    private boolean clientWritable;

    public FlowVariableInputDTO() {

    }

    public FlowVariableInputDTO(String name, FlowInputDataType dataType) {

        super(name, dataType);
    }

    /**
     * The initial value of the variable: a String, Long, Double or Boolean, or a list of Strings or Doubles.
     *
     * @return The initial value, or null when the variable starts without one.
     */
    public Object getValue() {

        return value;
    }

    public void setValue(Object value) {

        this.value = value;
    }

    public boolean isClientWritable() {

        return clientWritable;
    }

    public void setClientWritable(boolean clientWritable) {

        this.clientWritable = clientWritable;
    }
}
